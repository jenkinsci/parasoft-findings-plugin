/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import hudson.model.Job;
import hudson.model.Result;
import io.jenkins.plugins.util.EnvironmentResolver;
import io.jenkins.plugins.util.JenkinsFacade;
import org.apache.commons.lang3.StringUtils;

import com.parasoft.findings.jenkins.coverage.model.Metric;
import com.parasoft.findings.jenkins.coverage.model.Node;
import com.parasoft.findings.jenkins.coverage.model.Value;
import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;
import com.parasoft.findings.jenkins.coverage.api.metrics.source.SourceCodePainter;
import io.jenkins.plugins.forensics.delta.Delta;
import io.jenkins.plugins.forensics.delta.FileChanges;
import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.StageResultHandler;
import org.jvnet.localizer.Localizable;

import static com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder.PARASOFT_COVERAGE_NAME;
import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.DEFAULT_REFERENCE_BUILD_IDENTIFIER;
import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.ReferenceStatus.*;

/**
 * Transforms the old model to the new model and invokes all steps that work on the new model. Currently, only the
 * source code painting and copying has been moved to this new reporter class.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public class CoverageReporter {
    @SuppressWarnings("checkstyle:ParameterNumber")
    public CoverageBuildAction publishAction(final String id, final String icon, final Node rootNode,
                                             final Run<?, ?> build, final FilePath workspace, final TaskListener listener,
                                             final String configRefJob, final String configRefBuild,
                                             final List<CoverageQualityGate> qualityGates,
                                             final String sourceCodeEncoding, final StageResultHandler resultHandler)
            throws InterruptedException {
        FilteredLog log = new FilteredLog("Errors while reporting Parasoft code coverage result:");

        ReferenceBuildActionResult referenceBuildActionResult = getReferenceBuildActionResult(configRefJob, configRefBuild, build, id, log);
        CoverageBuildAction referenceAction = referenceBuildActionResult.getCoverageBuildAction();
        ReferenceResult referenceResult = referenceBuildActionResult.getReferenceResult();

        CoverageBuildAction action;
        if (referenceAction != null) {
            log.logInfo("Calculating the code delta...");
            CodeDeltaCalculator codeDeltaCalculator = new CodeDeltaCalculator(build, workspace, listener, StringUtils.EMPTY);
            Optional<Delta> delta = codeDeltaCalculator.calculateCodeDeltaToReference(referenceAction.getOwner(), log);
            delta.ifPresent(value -> createDeltaReports(rootNode, log, codeDeltaCalculator, value));

            Node modifiedLinesCoverageRoot = rootNode.filterByModifiedLines();
            if (!hasModifiedLinesCoverage(modifiedLinesCoverageRoot) && rootNode.hasModifiedLines()) {
                log.logInfo("No detected code changes affect the code coverage");
            }

            QualityGateResult qualityGateResult = evaluateQualityGates(rootNode, log,
                    modifiedLinesCoverageRoot.aggregateValues(), resultHandler, qualityGates);

            action = new CoverageBuildAction(build, id, icon, rootNode, qualityGateResult, log,
                    referenceAction.getOwner().getExternalizableId(), modifiedLinesCoverageRoot.aggregateValues(), referenceResult);
        }
        else {
            QualityGateResult qualityGateStatus = evaluateQualityGates(rootNode, log,
                    List.of(), resultHandler, qualityGates);

            action = new CoverageBuildAction(build, id, icon, rootNode, qualityGateStatus, log, referenceResult);
        }

        log.logInfo("Executing source code painting...");
        SourceCodePainter sourceCodePainter = new SourceCodePainter(build, workspace, id);
        sourceCodePainter.processSourceCodePainting(rootNode, rootNode.getAllFileNodes(),
                sourceCodeEncoding, SourceCodeRetention.LAST_BUILD, log);

        log.logInfo("Finished coverage processing - adding the action to the build...");

        LogHandler logHandler = new LogHandler(listener, PARASOFT_COVERAGE_NAME);
        logHandler.log(log);

        build.addAction(action);
        return action;
    }

    private void createDeltaReports(final Node rootNode, final FilteredLog log,
                                    final CodeDeltaCalculator codeDeltaCalculator, final Delta delta) {
        FileChangesProcessor fileChangesProcessor = new FileChangesProcessor();

        try {
            log.logInfo("Preprocessing code changes...");
            Set<FileChanges> changes = codeDeltaCalculator.getCoverageRelevantChanges(delta);
            var mappedChanges = codeDeltaCalculator.mapScmChangesToReportPaths(changes, rootNode, log);

            log.logInfo("Obtaining code changes for files...");
            fileChangesProcessor.attachChangedCodeLines(rootNode, mappedChanges);
        }
        catch (IllegalStateException exception) {
            log.logError("An error occurred while processing code and coverage changes:");
            log.logError("-> Message: " + exception.getMessage());
            log.logError("-> Skipping calculating modified lines coverage");
        }
    }

    private QualityGateResult evaluateQualityGates(final Node rootNode, final FilteredLog log,
                                                   final List<Value> modifiedLinesCoverageDistribution,
                                                   final StageResultHandler resultHandler,
                                                   final List<CoverageQualityGate> qualityGates) {
        var statistics = new CoverageStatistics(rootNode.aggregateValues(), modifiedLinesCoverageDistribution);
        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, statistics);
        var qualityGateStatus = evaluator.evaluate();
        if (qualityGateStatus.isInactive()) {
            log.logInfo("No quality gates have been set - skipping");
        }
        else {
            log.logInfo("Evaluating quality gates");
            if (qualityGateStatus.isSuccessful()) {
                log.logInfo("-> All quality gates passed");
            }
            else {
                var message = String.format("-> Some quality gates failed: overall result is %s",
                        qualityGateStatus.getOverallStatus().getResult());
                log.logInfo(message);
                resultHandler.setResult(qualityGateStatus.getOverallStatus().getResult(), message);
            }
            log.logInfo("-> Details for each quality gate:");
            qualityGateStatus.getMessages().forEach(log::logInfo);
        }
        return qualityGateStatus;
    }

    private boolean hasModifiedLinesCoverage(final Node modifiedLinesCoverageRoot) {
        Optional<Value> lineCoverage = modifiedLinesCoverageRoot.getValue(Metric.LINE);
        if (lineCoverage.isPresent() && hasLineCoverageSet(lineCoverage.get())) {
            return true;
        }
        Optional<Value> branchCoverage = modifiedLinesCoverageRoot.getValue(Metric.BRANCH);
        return branchCoverage.filter(this::hasLineCoverageSet).isPresent();
    }

    private boolean hasLineCoverageSet(final Value value) {
        return ((com.parasoft.findings.jenkins.coverage.model.Coverage) value).isSet();
    }

    private ReferenceBuildActionResult getReferenceBuildActionResult(final String configRefJob,
                                                                     final String configRefBuild,
                                                                     final Run<?, ?> build, final String id,
                                                                     final FilteredLog log) {
        log.logInfo("Obtaining action of reference build");
        boolean isDefaultJob = StringUtils.isBlank(configRefJob);
        Job<?,?> referenceJob;
        if (isDefaultJob) {
            referenceJob = build.getParent();
            log.logInfo("-> No reference job has been set; using build in current job '%s' as reference",
                    referenceJob.getFullName());
        } else {
            String expandedRefJob = expandConfigValue(build, configRefJob);
            if(!StringUtils.equals(expandedRefJob, configRefJob)) {
                log.logInfo("-> Expanding specified reference job '%s' to '%s'", configRefJob, expandedRefJob);
            }

            Optional<Job<?, ?>> possibleReferenceJob = new JenkinsFacade().getJob(expandedRefJob);
            if (possibleReferenceJob.isEmpty()) {
                logWarningMessage(Messages._Reference_Build_Warning_Message_NO_REF_JOB(expandedRefJob), log);
                return new ReferenceBuildActionResult(new ReferenceResult(NO_REF_JOB, expandedRefJob, configRefBuild));
            }

            referenceJob = possibleReferenceJob.get();
        }

        if (StringUtils.isBlank(configRefBuild)) {
            log.logInfo("-> No reference build has been set; using the last successful build in job '%s' as reference",
                    referenceJob.getFullName());
            return getDefaultReferenceBuildAction(referenceJob, isDefaultJob, build, id, log);
        } else {
            return getSpecifiedReferenceBuildAction(referenceJob, configRefBuild, build, id, log);
        }
    }

    private ReferenceBuildActionResult getDefaultReferenceBuildAction(final Job<?, ?> referenceJob,
                                                                      final boolean isDefaultJob,
                                                                      final Run<?, ?> build, final String id,
                                                                      final FilteredLog log) {
        boolean noPreviousBuild = isDefaultJob ? build.getPreviousBuild() == null : referenceJob.getLastBuild() == null;
        if (noPreviousBuild) {
            logWarningMessage(Messages._Reference_Build_Warning_Message_NO_PREVIOUS_BUILD_WAS_FOUND(
                    referenceJob.getFullName()), log);
            return new ReferenceBuildActionResult(new ReferenceResult(NO_PREVIOUS_BUILD_WAS_FOUND,
                    referenceJob.getFullName(), DEFAULT_REFERENCE_BUILD_IDENTIFIER));
        }

        Run<?, ?> previousSuccessfulBuild = referenceJob.getLastSuccessfulBuild();
        if (previousSuccessfulBuild == null) {
            logWarningMessage(Messages._Reference_Build_Warning_Message_NO_REF_BUILD(referenceJob.getFullName()), log);
            return new ReferenceBuildActionResult(new ReferenceResult(NO_REF_BUILD, referenceJob.getFullName(),
                    DEFAULT_REFERENCE_BUILD_IDENTIFIER));
        }

        Optional<CoverageBuildAction> previousSuccessfulResult;
        for (Run<?, ?> b = previousSuccessfulBuild; b != null; b = b.getPreviousSuccessfulBuild()) {
            previousSuccessfulResult = getCoverageResult(id, b);
            if (previousSuccessfulResult.isPresent()) {
                log.logInfo("-> Set build '%s' as the default reference build", b);
                return new ReferenceBuildActionResult(previousSuccessfulResult.get(),
                        new ReferenceResult(OK, b.getExternalizableId()));
            }
        }

        logWarningMessage(Messages._Reference_Build_Warning_Message_NO_CVG_DATA_IN_PREVIOUS_SUCCESSFUL_BUILDS(
                referenceJob.getFullName()), log);
        return new ReferenceBuildActionResult(new ReferenceResult(NO_CVG_DATA_IN_REF_BUILD, referenceJob.getFullName(),
                DEFAULT_REFERENCE_BUILD_IDENTIFIER));
    }

    private ReferenceBuildActionResult getSpecifiedReferenceBuildAction(final Job<?, ?> referenceJob,
                                                                        final String configRefBuild,
                                                                        final Run<?, ?> build, final String id,
                                                                        final FilteredLog log) {
        String expandedRefBuild = expandConfigValue(build, configRefBuild);
        if(!StringUtils.equals(expandedRefBuild, configRefBuild)) {
            log.logInfo("-> Expanding specified reference build '%s' to '%s'", configRefBuild, expandedRefBuild);
        }

        Run<?, ?> referenceBuild = getSpecifiedReferenceBuild(referenceJob, expandedRefBuild, log);
        if (referenceBuild == null) {
            logWarningMessage(Messages._Reference_Build_Warning_Message_NO_SPECIFIED_REF_BUILD(expandedRefBuild,
                    referenceJob.getFullName()), log);
            return new ReferenceBuildActionResult(new ReferenceResult(NO_REF_BUILD, referenceJob.getFullName(),
                    expandedRefBuild));
        }

        if (StringUtils.equals(build.getExternalizableId(), referenceBuild.getExternalizableId())) {
            logWarningMessage(Messages._Reference_Build_Warning_Message_REF_BUILD_IS_CURRENT_BUILD(
                    referenceBuild), log);
            return new ReferenceBuildActionResult(new ReferenceResult(REF_BUILD_IS_CURRENT_BUILD,
                    referenceBuild.getExternalizableId()));
        }

        return getReferenceBuildAction(referenceBuild, id, log);
    }

    private ReferenceBuildActionResult getReferenceBuildAction(final Run<?, ?> referenceBuild, final String id,
                                                               final FilteredLog log) {
        Result result = referenceBuild.getResult();
        if (result == Result.SUCCESS || result == Result.UNSTABLE) {
            Optional<CoverageBuildAction> coverageResult = getCoverageResult(id, referenceBuild);

            if (coverageResult.isEmpty()) {
                logWarningMessage(Messages._Reference_Build_Warning_Message_NO_CVG_DATA_IN_REF_BUILD(
                        referenceBuild), log);
                return new ReferenceBuildActionResult(new ReferenceResult(NO_CVG_DATA_IN_REF_BUILD,
                        referenceBuild.getExternalizableId()));
            }

            log.logInfo("-> Retrieved Parasoft code coverage result from the reference build '%s'", referenceBuild);
            return new ReferenceBuildActionResult(coverageResult.get(),
                    new ReferenceResult(OK, referenceBuild.getExternalizableId()));
        } else {
            logWarningMessage(Messages._Reference_Build_Warning_Message_REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE(
                    referenceBuild), log);
            return new ReferenceBuildActionResult(new ReferenceResult(REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE,
                    referenceBuild.getExternalizableId()));
        }
    }

    private Run<?, ?> getSpecifiedReferenceBuild(final Job<?, ?> job, final String expandedRefBuild,
                                                 final FilteredLog log) {
        if (!StringUtils.isNumeric(expandedRefBuild)) {
            log.logInfo("-> The specified reference build number '%s' is invalid", expandedRefBuild);
            return null;
        }

        try {
            return job.getBuildByNumber(Integer.parseInt(expandedRefBuild));
        } catch (NumberFormatException nfe) {
            log.logInfo("-> The specified reference build number '%s' is out of range", expandedRefBuild);
            return null;
        }
    }

    private Optional<CoverageBuildAction> getCoverageResult(final String id, @CheckForNull final Run<?, ?> build) {
        if (build == null) {
            return Optional.empty();
        }
        List<CoverageBuildAction> actions = build.getActions(CoverageBuildAction.class);
        for (CoverageBuildAction action : actions) {
            if (action.getUrlName().equals(id)) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }

    private String expandConfigValue(final Run<?, ?> run, final String configValue) {
        try {
            EnvironmentResolver environmentResolver = new EnvironmentResolver();

            return environmentResolver.expandEnvironmentVariables(
                    run.getEnvironment(TaskListener.NULL), configValue);
        } catch (IOException | InterruptedException ignore) {
            return configValue; // fallback, no expansion
        }
    }

    private static void logWarningMessage(Localizable message, final FilteredLog log) {
        log.logInfo("-> %s", message.toString(Locale.ROOT));
    }
}
