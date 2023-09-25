package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.coverage.Value;
import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;
import com.parasoft.findings.jenkins.coverage.api.metrics.source.SourceCodePainter;
import io.jenkins.plugins.forensics.delta.Delta;
import io.jenkins.plugins.forensics.delta.FileChanges;
import io.jenkins.plugins.forensics.reference.ReferenceFinder;
import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.StageResultHandler;

/**
 * Transforms the old model to the new model and invokes all steps that work on the new model. Currently, only the
 * source code painting and copying has been moved to this new reporter class.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public class CoverageReporter {
    @SuppressWarnings("checkstyle:ParameterNumber")
    public CoverageBuildAction publishAction(final String id, final String optionalName, final String icon, final Node rootNode,
                                             final Run<?, ?> build, final FilePath workspace, final TaskListener listener,
                                             final List<CoverageQualityGate> qualityGates, final String scm, final String sourceCodeEncoding,
                                             final SourceCodeRetention sourceCodeRetention, final StageResultHandler resultHandler)
            throws InterruptedException {
        FilteredLog log = new FilteredLog("Errors while reporting code coverage results:");

        Optional<CoverageBuildAction> possibleReferenceResult = getReferenceBuildAction(build, id, log);

        List<FileNode> filesToStore;
        CoverageBuildAction action;
        if (possibleReferenceResult.isPresent()) {
            CoverageBuildAction referenceAction = possibleReferenceResult.get();
            Node referenceRoot = referenceAction.getResult();

            log.logInfo("Calculating the code delta...");
            CodeDeltaCalculator codeDeltaCalculator = new CodeDeltaCalculator(build, workspace, listener, scm);
            Optional<Delta> delta = codeDeltaCalculator.calculateCodeDeltaToReference(referenceAction.getOwner(), log);
            delta.ifPresent(value -> createDeltaReports(rootNode, log, referenceRoot, codeDeltaCalculator, value));

            log.logInfo("Calculating coverage deltas...");

            Node modifiedLinesCoverageRoot = rootNode.filterByModifiedLines();

            if (rootNode.hasModifiedLines()) {
                log.logInfo("No detected code changes affect the code coverage");
            }

            QualityGateResult qualityGateResult = evaluateQualityGates(rootNode, log,
                    modifiedLinesCoverageRoot.aggregateValues(), resultHandler, qualityGates);

            if (sourceCodeRetention == SourceCodeRetention.MODIFIED) {
                filesToStore = modifiedLinesCoverageRoot.getAllFileNodes();
                log.logInfo("-> Selecting %d modified files for source code painting", filesToStore.size());
            }
            else {
                filesToStore = rootNode.getAllFileNodes();
            }

            action = new CoverageBuildAction(build, id, optionalName, icon, rootNode, qualityGateResult, log,
                    referenceAction.getOwner().getExternalizableId(), modifiedLinesCoverageRoot.aggregateValues());
        }
        else {
            QualityGateResult qualityGateStatus = evaluateQualityGates(rootNode, log,
                    List.of(), resultHandler, qualityGates);

            filesToStore = rootNode.getAllFileNodes();

            action = new CoverageBuildAction(build, id, optionalName, icon, rootNode, qualityGateStatus, log);
        }

        log.logInfo("Executing source code painting...");
        SourceCodePainter sourceCodePainter = new SourceCodePainter(build, workspace, id);
        sourceCodePainter.processSourceCodePainting(rootNode, filesToStore,
                sourceCodeEncoding, sourceCodeRetention, log);

        log.logInfo("Finished coverage processing - adding the action to the build...");

        LogHandler logHandler = new LogHandler(listener, "Coverage");
        logHandler.log(log);

        build.addAction(action);
        return action;
    }

    private void createDeltaReports(final Node rootNode, final FilteredLog log, final Node referenceRoot,
                                    final CodeDeltaCalculator codeDeltaCalculator, final Delta delta) {
        FileChangesProcessor fileChangesProcessor = new FileChangesProcessor();

        try {
            log.logInfo("Preprocessing code changes...");
            Set<FileChanges> changes = codeDeltaCalculator.getCoverageRelevantChanges(delta);
            var mappedChanges = codeDeltaCalculator.mapScmChangesToReportPaths(changes, rootNode, log);
            var oldPathMapping = codeDeltaCalculator.createOldPathMapping(rootNode, referenceRoot, mappedChanges, log);

            log.logInfo("Obtaining code changes for files...");
            fileChangesProcessor.attachChangedCodeLines(rootNode, mappedChanges);

            log.logInfo("Obtaining coverage delta for files...");
            fileChangesProcessor.attachFileCoverageDeltas(rootNode, referenceRoot, oldPathMapping);
        }
        catch (IllegalStateException exception) {
            log.logError("An error occurred while processing code and coverage changes:");
            log.logError("-> Message: " + exception.getMessage());
            log.logError("-> Skipping calculating modified lines coverage and modified files coverage");
        }
    }

    private QualityGateResult evaluateQualityGates(final Node rootNode, final FilteredLog log,
                                                   final List<Value> modifiedLinesCoverageDistribution,
                                                   final StageResultHandler resultHandler,
                                                   final List<CoverageQualityGate> qualityGates) {
        var statistics = new CoverageStatistics(rootNode.aggregateValues(),
                modifiedLinesCoverageDistribution);
        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, statistics);
        var qualityGateStatus = evaluator.evaluate();
        if (qualityGateStatus.isInactive()) {
            log.logInfo("No quality gates have been set - skipping");
        }
        else {
            log.logInfo("Evaluating quality gates");
            if (qualityGateStatus.isSuccessful()) {
                log.logInfo("-> All quality gates have been passed");
            }
            else {
                var message = String.format("-> Some quality gates have been missed: overall result is %s",
                        qualityGateStatus.getOverallStatus().getResult());
                log.logInfo(message);
                resultHandler.setResult(qualityGateStatus.getOverallStatus().getResult(), message);
            }
            log.logInfo("-> Details for each quality gate:");
            qualityGateStatus.getMessages().forEach(log::logInfo);
        }
        return qualityGateStatus;
    }
    private Optional<CoverageBuildAction> getReferenceBuildAction(final Run<?, ?> build, final String id, final FilteredLog log) {
        log.logInfo("Obtaining action of reference build");

        ReferenceFinder referenceFinder = new ReferenceFinder();
        Optional<Run<?, ?>> reference = referenceFinder.findReference(build, log);

        Optional<CoverageBuildAction> previousResult;
        if (reference.isPresent()) {
            Run<?, ?> referenceBuild = reference.get();
            log.logInfo("-> Using reference build '%s'", referenceBuild);
            previousResult = getPreviousResult(id, reference.get());
            if (previousResult.isPresent()) {
                Run<?, ?> fallbackBuild = previousResult.get().getOwner();
                if (!fallbackBuild.equals(referenceBuild)) {
                    log.logInfo("-> Reference build has no action, falling back to last build with action: '%s'",
                            fallbackBuild.getDisplayName());
                }
            }
        }
        else {
            previousResult = getPreviousResult(id, build.getPreviousBuild());
            previousResult.ifPresent(coverageBuildAction ->
                    log.logInfo("-> No reference build defined, falling back to previous build: '%s'",
                            coverageBuildAction.getOwner().getDisplayName()));
        }

        if (previousResult.isEmpty()) {
            log.logInfo("-> Found no reference result in reference build");

            return Optional.empty();
        }

        CoverageBuildAction referenceAction = previousResult.get();
        log.logInfo("-> Found reference result in build '%s'", referenceAction.getOwner().getDisplayName());

        return Optional.of(referenceAction);
    }

    private Optional<CoverageBuildAction> getPreviousResult(final String id, @CheckForNull final Run<?, ?> startSearch) {
        for (Run<?, ?> build = startSearch; build != null; build = build.getPreviousBuild()) {
            List<CoverageBuildAction> actions = build.getActions(CoverageBuildAction.class);
            for (CoverageBuildAction action : actions) {
                if (action.getUrlName().equals(id)) {
                    return Optional.of(action);
                }
            }
        }
        return Optional.empty();
    }
}
