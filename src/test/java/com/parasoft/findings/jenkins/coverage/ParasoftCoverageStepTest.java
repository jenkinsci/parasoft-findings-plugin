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
    void testInvalidReferenceBuild() {
        WorkflowJob job = createPipeline("abc", UNSTABLE_COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build = buildWithResult(job, Result.UNSTABLE);

        verifyAction(build.getAction(CoverageBuildAction.class));
        assertThat(getConsoleLog(build)).contains("The specified reference build number 'abc' is invalid");
    }

    @Test
    void testNoCoverageFilesFound() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, "wrongFile.xml");
        Run<?, ?> build = buildWithResult(job, Result.FAILURE);

        assertThat(getConsoleLog(build)).contains("[-ERROR-] No files found for pattern 'wrongFile.xml'. Configuration error?");
    }

    @Test
    void testNoCoverageDataFound() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, "parasoft_coverage_no_data.xml");
        Run<?, ?> build = buildWithResult(job, Result.FAILURE);

        assertThat(getConsoleLog(build)).contains("No Parasoft coverage information found in the specified file.");
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
    void testWhenReferenceJobIsNotSet() {
        WorkflowJob job = createPipeline(null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        buildSuccessfully(job);
        Run<?, ?> currentBuild = buildSuccessfully(job);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains(String.format("No reference job has been set; using build in current job '%s' as reference", currentBuild.getParent().getFullName()));
    }

    @Test
    void testWhenReferenceBuildIsSet() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        buildSuccessfully(job);
        Run<?, ?> currentBuild = buildSuccessfully(job);
        var actions = currentBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains("Obtaining action of reference build");
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
