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
        assertThat(getConsoleLog(build)).contains("Found no specified reference build '7'");
        assertThat(getConsoleLog(build)).contains("Source file '/workspace/test0/com/parasoft/interfaces2/ICalculator.java' not found");
    }

    @Test
    void testInvalidReferenceBuild() {
        WorkflowJob job = createPipeline("abc", UNSTABLE_COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        Run<?, ?> build = buildWithResult(job, Result.UNSTABLE);

        verifyAction(build.getAction(CoverageBuildAction.class));
        assertThat(getConsoleLog(build)).contains("Invalid reference build number 'abc'");
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
        System.out.println(coverageResult.getAllValues(Baseline.PROJECT).toString());
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .contains(new Coverage.CoverageBuilder().setMetric(Metric.LINE).setCovered(COVERED_LINES)
                        .setMissed(MISSED_LINES)
                        .build());
    }
}
