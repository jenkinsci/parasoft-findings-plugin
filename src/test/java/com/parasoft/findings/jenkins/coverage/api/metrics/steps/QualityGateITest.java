package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageITest;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTool.Parser;
import edu.hm.hafner.coverage.Metric;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest.JACOCO_ANALYSIS_MODEL_FILE;
import static io.jenkins.plugins.util.assertions.Assertions.assertThat;

/**
 * Integration tests with active quality gates.
 */
class QualityGateITest extends AbstractCoverageITest {

    @Test
    void shouldPassQualityGate() {
        var qualityGates = List.of(new CoverageQualityGate(-100.0, Metric.LINE, Baseline.PROJECT, QualityGateCriticality.UNSTABLE));
        FreeStyleProject project = createFreestyleJob(Parser.JACOCO, r -> r.setQualityGates(qualityGates), JACOCO_ANALYSIS_MODEL_FILE);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        CoverageBuildAction coverageResult = build.getAction(CoverageBuildAction.class);
        assertThat(coverageResult.getQualityGateResult()).hasOverallStatus(QualityGateStatus.PASSED);
    }

    @Test
    void shouldFailQualityGateWithFailure() {
        var qualityGates = List.of(new CoverageQualityGate(100, Metric.LINE, Baseline.PROJECT, QualityGateCriticality.FAILURE));
        FreeStyleProject project = createFreestyleJob(Parser.JACOCO, r -> r.setQualityGates(qualityGates), JACOCO_ANALYSIS_MODEL_FILE);

        Run<?, ?> build = buildWithResult(project, Result.FAILURE);

        CoverageBuildAction coverageResult = build.getAction(CoverageBuildAction.class);
        assertThat(coverageResult.getQualityGateResult()).hasOverallStatus(QualityGateStatus.FAILED);
    }
}
