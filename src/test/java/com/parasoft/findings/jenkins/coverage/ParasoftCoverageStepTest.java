package com.parasoft.findings.jenkins.coverage;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageITest;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageBuildAction;
import com.parasoft.findings.jenkins.coverage.model.Coverage;
import com.parasoft.findings.jenkins.coverage.model.Metric;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParasoftCoverageStepTest extends AbstractCoverageITest {
    private static final String COVERAGE_FILE = "parasoft_coverage.xml";
    private static final String COVERAGE_QUALITY_GATE_SCRIPT = "[type: 'PROJECT', criticality: 'UNSTABLE', threshold: 0.0]";
    private static final String UNSTABLE_COVERAGE_QUALITY_GATE_SCRIPT = "[type: 'PROJECT', criticality: 'UNSTABLE', threshold: 100.0]";
    private static final String SOURCECODE_ENCODING = "UTF-8";
    private static final int COVERED_LINES = 28;
    private static final int MISSED_LINES = 8;

    @Test
    void testJobWithAllParameters() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        verifyResult(job);
    }

    @Test
    void testJobWithOutReferenceJob() {
        WorkflowJob job = createPipeline(null, null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        verifyResult(job);
    }

    @Test
    void testJobWithOutReferenceBuild() {
        WorkflowJob job = createPipeline(null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        verifyResult(job);
    }

    @Test
    void testJobWithOutCoverageQualityGates() {
        WorkflowJob job = createPipeline("1", null, SOURCECODE_ENCODING,COVERAGE_FILE);
        verifyResult(job);
    }

    @Test
    void testJobWithOutsourceCodeEncoding() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, null, COVERAGE_FILE);
        verifyResult(job);
    }

    @Test
    void testNotPassTheQualityGate() {
        WorkflowJob job = createPipeline("7", UNSTABLE_COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build = buildWithResult(job, Result.UNSTABLE);

        verifyAction(build.getAction(CoverageBuildAction.class));
        assertThat(getConsoleLog(build)).contains("Some quality gates failed: overall result is UNSTABLE");
        assertThat(getConsoleLog(build)).contains(String.format("The specified reference build '7' could not be found in job '%s'", build.getParent().getFullName()));
        assertThat(getConsoleLog(build)).contains("Source file '/workspace/test0/com/parasoft/interfaces2/ICalculator.java' not found");
    }

    @Test
    void testInvalidReferenceBuildNumber() {
        final String invalidReferenceBuildNumber = "abc";
        WorkflowJob job = createPipeline(invalidReferenceBuildNumber, UNSTABLE_COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build = buildWithResult(job, Result.UNSTABLE);

        verifyAction(build.getAction(CoverageBuildAction.class));
        assertThat(getConsoleLog(build)).contains(
                String.format("The specified reference build number '%s' is invalid", invalidReferenceBuildNumber));

        final String outOfRangeReferenceBuildNumber = String.valueOf((long) Integer.MAX_VALUE + 1);
        WorkflowJob job1 = createPipeline(outOfRangeReferenceBuildNumber, UNSTABLE_COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build1 = buildWithResult(job1, Result.UNSTABLE);

        verifyAction(build1.getAction(CoverageBuildAction.class));
        assertThat(getConsoleLog(build1)).contains(
                String.format("The specified reference build number '%s' is out of range", outOfRangeReferenceBuildNumber));
    }

    @Test
    void testNoCoverageFilesFound() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, "wrongFile.xml");
        Run<?, ?> build = buildWithResult(job, Result.FAILURE);

        assertThat(getConsoleLog(build)).contains("[-ERROR-] No files found for pattern 'wrongFile.xml'. Configuration error?");

        WorkflowJob currentJob = createPipeline(job.getFullName(), null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> currentBuild = buildSuccessfully(currentJob);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains(
                String.format("No stable build was found in job '%s'", job.getFullName()));
    }

    @Test
    void testNoCoverageDataFound() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, "parasoft_coverage_no_data.xml");
        Run<?, ?> build = buildWithResult(job, Result.FAILURE);

        assertThat(getConsoleLog(build)).contains("No Parasoft coverage information found in the specified file.");
    }

    @Test
    void testNoCoverageDataInReferenceBuild() {
        WorkflowJob referenceJob = createPipeline();
        setPipelineScript(referenceJob, "");
        Run<?, ?> referenceBuild = buildSuccessfully(referenceJob);

        WorkflowJob job1 = createPipeline(referenceJob.getFullName(), null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build1 = buildSuccessfully(job1);
        var actions1 = build1.getActions(CoverageBuildAction.class);
        var result1 = actions1.get(0);
        assertThat(result1.getLog().getInfoMessages().toString()).contains(
                String.format("No Parasoft code coverage result was found in any of the previous stable builds in job '%s'",
                        referenceJob.getFullName()));

        WorkflowJob job2 = createPipeline(referenceJob.getFullName(), referenceBuild.getId(), COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build2 = buildSuccessfully(job2);
        var actions2 = build2.getActions(CoverageBuildAction.class);
        var result2 = actions2.get(0);
        assertThat(result2.getLog().getInfoMessages().toString()).contains(
                String.format("No Parasoft code coverage result was found in reference build '%s'", referenceBuild));
    }

    private void verifyResult(final ParameterizedJobMixIn.ParameterizedJob<?, ?> project) {
        Run<?, ?> build = buildSuccessfully(project);

        verifyAction(build.getAction(CoverageBuildAction.class));
    }

    private static void verifyAction(final CoverageBuildAction coverageResult) {
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .contains(new Coverage.CoverageBuilder().setMetric(Metric.LINE).setCovered(COVERED_LINES)
                        .setMissed(MISSED_LINES)
                        .build());
    }

    @Test
    void testWhenBothReferenceJobAndReferenceBuildAreNotSet() {
        WorkflowJob job = createPipeline(null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> previousBuild = buildSuccessfully(job);
        var previousActions = previousBuild.getActions(CoverageBuildAction.class);
        var previousResult = previousActions.get(0);
        assertThat(previousResult.getLog().getInfoMessages().toString()).contains(
                String.format("No reference job has been set; using build in current job '%s' as reference",
                        job.getFullName()),
                String.format("No reference build has been set; using the last stable build in job '%s' as reference",
                        job.getFullName()),
                String.format("No previous build was found in job '%s'", job.getFullName()));

        Run<?, ?> currentBuild = buildSuccessfully(job);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains(
                String.format("Set build '%s' as the default reference build", previousBuild));
    }

    @Test
    void testWhenBothReferenceJobAndReferenceBuildAreSet() {
        final String notExistJobName = "not-exist-job";
        WorkflowJob referenceJob = createPipeline(notExistJobName, null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> referenceBuild = buildSuccessfully(referenceJob);
        var referenceActions = referenceBuild.getActions(CoverageBuildAction.class);
        var referenceResult = referenceActions.get(0);
        assertThat(referenceResult.getLog().getInfoMessages().toString()).contains(
                String.format("The specified reference job '%s' could not be found", notExistJobName));

        WorkflowJob job = createPipeline(referenceJob.getFullName(), referenceBuild.getId(), COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> currentBuild = buildSuccessfully(job);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains(
                String.format("Retrieved Parasoft code coverage result from the reference build '%s'", referenceBuild));
    }

    @Test
    void testWhenOnlyReferenceJobOrReferenceBuildIsSet() {
        final String specifiedReferenceBuildNumber = "1";
        WorkflowJob referenceJob = createPipeline(specifiedReferenceBuildNumber, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> referenceBuild = buildSuccessfully(referenceJob);
        var referenceActions = referenceBuild.getActions(CoverageBuildAction.class);
        var referenceResult = referenceActions.get(0);
        assertThat(referenceResult.getLog().getInfoMessages().toString()).contains(
                String.format("The reference build '%s' was ignored since the build number set is same as the current build",
                        referenceBuild));

        WorkflowJob job = createPipeline(referenceJob.getFullName(), null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> currentBuild = buildSuccessfully(job);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains(
                String.format("Set build '%s' as the default reference build", referenceBuild));
    }

    @Test
    void testWhenReferenceBuildIsFailed() {
        WorkflowJob job = createPipelineWithWorkspaceFiles(COVERAGE_FILE, "parasoft_coverage_no_data.xml");
        setPipelineScript(job,
                "recordParasoftCoverage coverageQualityGates: [" + COVERAGE_QUALITY_GATE_SCRIPT + "], " + " referenceBuild: '1', pattern: '" + "parasoft_coverage_no_data.xml" + "', sourceCodeEncoding: '" + SOURCECODE_ENCODING + "'");
        buildWithResult(job, Result.FAILURE);
        setPipelineScript(job,
                "recordParasoftCoverage coverageQualityGates: [" + COVERAGE_QUALITY_GATE_SCRIPT + "], " + " referenceBuild: '1', pattern: '" + COVERAGE_FILE + "', sourceCodeEncoding: '" + SOURCECODE_ENCODING + "'");
        Run<?, ?> currentBuild = buildSuccessfully(job);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains(String.format("The reference build '%s #1' cannot be used. Only successful or unstable builds are valid references", currentBuild.getParent().getFullName()));
    }
}
