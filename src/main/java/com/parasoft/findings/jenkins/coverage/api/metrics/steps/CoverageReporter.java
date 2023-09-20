package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import hudson.model.Job;
import hudson.model.PermalinkProjectAction;
import io.jenkins.plugins.util.EnvironmentResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.Fraction;

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
import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.StageResultHandler;

import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageBuildAction.NO_REFERENCE_BUILD;
import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageQualityGate.DEFAULT_REFERENCE_BUILD;
import static java.util.stream.Collectors.groupingBy;

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

        Map<String, List<CoverageQualityGate>> qualityGatesGroupedByActualBuildId = new TreeMap<>();
        Map<String, Optional<CoverageBuildAction>> possibleRefResultsPerActualBuildId = new TreeMap<>();

        groupQualityGatesByActualRefBuildId(qualityGatesGroupedByActualBuildId,
                possibleRefResultsPerActualBuildId, qualityGates, build, id, log);

        qualityGatesGroupedByActualBuildId.forEach((actualRefBuildId, coverageQualityGates) -> {
            // Calculate modifiedLinesCoverage, evaluate quality gates for every actual reference build.
        });

        // Only one of the reference build is used, because the logic below this can only handle one for now.
        Optional<CoverageBuildAction> possibleReferenceResult = !possibleRefResultsPerActualBuildId.isEmpty()
                ? possibleRefResultsPerActualBuildId.values().stream().findAny().get()
                : Optional.empty();

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

            NavigableMap<Metric, Fraction> modifiedLinesCoverageDelta;
            List<Value> aggregatedModifiedFilesCoverage;
            NavigableMap<Metric, Fraction> modifiedFilesCoverageDelta;
            if (hasModifiedLinesCoverage(modifiedLinesCoverageRoot)) {
                Node modifiedFilesCoverageRoot = rootNode.filterByModifiedFiles();
                aggregatedModifiedFilesCoverage = modifiedFilesCoverageRoot.aggregateValues();
                modifiedFilesCoverageDelta = modifiedFilesCoverageRoot.computeDelta(rootNode);
                modifiedLinesCoverageDelta = modifiedLinesCoverageRoot.computeDelta(modifiedFilesCoverageRoot);
            }
            else {
                modifiedLinesCoverageDelta = new TreeMap<>();
                aggregatedModifiedFilesCoverage = new ArrayList<>();
                modifiedFilesCoverageDelta = new TreeMap<>();
                if (rootNode.hasModifiedLines()) {
                    log.logInfo("No detected code changes affect the code coverage");
                }
            }

            NavigableMap<Metric, Fraction> coverageDelta = rootNode.computeDelta(referenceRoot);

            QualityGateResult qualityGateResult = evaluateQualityGates(rootNode, log,
                    modifiedLinesCoverageRoot.aggregateValues(), modifiedLinesCoverageDelta, coverageDelta,
                    resultHandler, qualityGates);

            if (sourceCodeRetention == SourceCodeRetention.MODIFIED) {
                filesToStore = modifiedLinesCoverageRoot.getAllFileNodes();
                log.logInfo("-> Selecting %d modified files for source code painting", filesToStore.size());
            }
            else {
                filesToStore = rootNode.getAllFileNodes();
            }

            action = new CoverageBuildAction(build, id, optionalName, icon, rootNode, qualityGateResult, log,
                    referenceAction.getOwner().getExternalizableId(), coverageDelta,
                    modifiedLinesCoverageRoot.aggregateValues(), modifiedLinesCoverageDelta,
                    aggregatedModifiedFilesCoverage, modifiedFilesCoverageDelta);
        }
        else {
            QualityGateResult qualityGateStatus = evaluateQualityGates(rootNode, log,
                    List.of(), new TreeMap<>(), new TreeMap<>(), resultHandler, qualityGates);

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
            final NavigableMap<Metric, Fraction> modifiedLinesCoverageDelta,
            final NavigableMap<Metric, Fraction> coverageDelta, final StageResultHandler resultHandler,
            final List<CoverageQualityGate> qualityGates) {
        var statistics = new CoverageStatistics(rootNode.aggregateValues(), coverageDelta,
                modifiedLinesCoverageDistribution, modifiedLinesCoverageDelta, List.of(), new TreeMap<>());
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

    private boolean hasModifiedLinesCoverage(final Node modifiedLinesCoverageRoot) {
        Optional<Value> lineCoverage = modifiedLinesCoverageRoot.getValue(Metric.LINE);
        if (lineCoverage.isPresent() && hasLineCoverageSet(lineCoverage.get())) {
            return true;
        }
        Optional<Value> branchCoverage = modifiedLinesCoverageRoot.getValue(Metric.BRANCH);
        return branchCoverage.filter(this::hasLineCoverageSet).isPresent();
    }

    private boolean hasLineCoverageSet(final Value value) {
        return ((edu.hm.hafner.coverage.Coverage) value).isSet();
    }

    private void groupQualityGatesByActualRefBuildId(
            Map<String, List<CoverageQualityGate>> qualityGatesGroupedByActualBuildId,
                     Map<String, Optional<CoverageBuildAction>> possibleRefResultsPerActualBuildId,
                     final List<CoverageQualityGate> qualityGates,
                     final Run<?, ?> build, final String id, final FilteredLog log) {

        Map<String, List<CoverageQualityGate>> qualityGatesGroupedByConfigRefBuild =
                qualityGates.stream().collect(groupingBy(qualityGate -> {
                    if (StringUtils.isBlank(qualityGate.getReferenceBuild())) {
                        log.logInfo("Using default reference build '%s' for quality gate '%s' since user defined " +
                                "reference build is not set", DEFAULT_REFERENCE_BUILD.getId(), qualityGate);
                    }
                    return qualityGate.getActualReferenceBuild();
                }));

        Map<String, String> configRefBuildActualBuildIdMap = new TreeMap<>();
        qualityGatesGroupedByConfigRefBuild.forEach((configRefBuild, coverageQualityGates) -> {
            String actualBuildId = NO_REFERENCE_BUILD;

            // When the quality gate baseline type is PROJECT, its reference build is always '-'(NO_REFERENCE_BUILD).
            if (StringUtils.equals(configRefBuild, NO_REFERENCE_BUILD)) {
                configRefBuildActualBuildIdMap.put(configRefBuild, actualBuildId);
            } else {
                if (configRefBuildActualBuildIdMap.containsKey(configRefBuild)) {
                    actualBuildId = configRefBuildActualBuildIdMap.get(configRefBuild);
                } else {
                    Optional<CoverageBuildAction> referenceAction =
                            getReferenceBuildAction(build, configRefBuild, id, log);
                    if(referenceAction.isPresent()) {
                        actualBuildId = referenceAction.get().getOwner().getExternalizableId();
                    }
                    configRefBuildActualBuildIdMap.put(configRefBuild, actualBuildId);
                    possibleRefResultsPerActualBuildId.putIfAbsent(actualBuildId, referenceAction);
                }
            }

            List<CoverageQualityGate> qualityGatesOfActualBuildId = qualityGatesGroupedByActualBuildId
                    .getOrDefault(actualBuildId, new ArrayList<>());
            qualityGatesOfActualBuildId.addAll(coverageQualityGates);
            qualityGatesGroupedByActualBuildId.put(actualBuildId, qualityGatesOfActualBuildId);
        });
    }

    private Optional<CoverageBuildAction> getReferenceBuildAction(final Run<?, ?> build,
                                                                                final String configRefBuild,
                                                                                final String id, final FilteredLog log) {
        log.logInfo("Obtaining action of reference build '%s'", configRefBuild);

        Optional<Run<?, ?>> reference = getReferenceBuild(build, configRefBuild, log);

        Optional<CoverageBuildAction> previousResult;
        if (reference.isPresent()) {
            Run<?, ?> referenceBuild = reference.get();
            log.logInfo("-> Using reference build '%s'", referenceBuild);
            previousResult = getPreviousResult(id, referenceBuild);

            if (previousResult.isEmpty()) {
                log.logInfo("-> Found no reference result in reference build '%s'", referenceBuild);
                return Optional.empty();
            }

            CoverageBuildAction referenceAction = previousResult.get();
            log.logInfo("-> Found reference result in build '%s'", referenceBuild);
            return Optional.of(referenceAction);
        } else {
            log.logInfo("-> No reference build defined for '%s'", configRefBuild);
            return Optional.empty();
        }
    }

    private Optional<CoverageBuildAction> getPreviousResult(final String id, @CheckForNull final Run<?, ?> build) {
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

    private Optional<Run<?, ?>> getReferenceBuild(final Run<?, ?> currentBuild, final String configRefBuild,
                                                  final FilteredLog log) {
        String expandedConfigRefBuild = expandReferenceBuild(currentBuild, configRefBuild);
        if(!StringUtils.equals(expandedConfigRefBuild, configRefBuild)) {
            log.logInfo("-> Expanding reference build '%s' to '%s'", configRefBuild, expandedConfigRefBuild);
        }

        Run<?,?> run = null;

        Job<?, ?> job = currentBuild.getParent();
        if (StringUtils.isNumeric(expandedConfigRefBuild)) {
            try {
                run = job.getBuildByNumber(Integer.parseInt(expandedConfigRefBuild));
            } catch (NumberFormatException nfe) {
                log.logInfo("-> Invalid build number '%s'", expandedConfigRefBuild);
            }
        } else {
            PermalinkProjectAction.Permalink p = job.getPermalinks().get(expandedConfigRefBuild);
            if (p != null) {
                run = p.resolve(job);
            }
        }

        if (run == null) {
            log.logInfo("-> No such build '%s'", expandedConfigRefBuild);
            return Optional.empty();
        }

        if (StringUtils.equals(currentBuild.getExternalizableId(), run.getExternalizableId())) {
            log.logInfo("-> Can not use current build '%s' as reference build", expandedConfigRefBuild);
            return Optional.empty();
        }

        return Optional.of(run);
    }

    private String expandReferenceBuild(final Run<?, ?> run, final String actualReferenceBuild) {
        try {
            EnvironmentResolver environmentResolver = new EnvironmentResolver();

            return environmentResolver.expandEnvironmentVariables(
                    run.getEnvironment(TaskListener.NULL), actualReferenceBuild);
        }
        catch (IOException | InterruptedException ignore) {
            return actualReferenceBuild; // fallback, no expansion
        }
    }
}
