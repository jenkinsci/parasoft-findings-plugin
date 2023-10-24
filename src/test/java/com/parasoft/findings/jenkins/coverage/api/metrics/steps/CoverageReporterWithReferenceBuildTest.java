package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageITest;

public class CoverageReporterWithReferenceBuildTest extends AbstractCoverageITest {
    private static final String COVERAGE_QUALITY_GATE_SCRIPT = "[type: 'PROJECT', criticality: 'UNSTABLE', threshold: 0.0]";
    private static final String COVERAGE_FILE = "parasoft_coverage.xml";
    private static final String SOURCECODE_ENCODING = "UTF-8";
    @Test
    void testWhenReferenceBuildIsNotSet() {
        WorkflowJob job = createPipeline(null, COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        buildSuccessfully(job);
        Run<?, ?> secondBuild = buildSuccessfully(job);
        var actions = secondBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains("Using default reference build(last successful build with code coverage data) ");
    }

    @Test
    void testWhenReferenceBuildIsSet() {
        WorkflowJob job = createPipeline("1", COVERAGE_QUALITY_GATE_SCRIPT, SOURCECODE_ENCODING, COVERAGE_FILE);
        buildSuccessfully(job);
        Run<?, ?> secondBuild = buildSuccessfully(job);
        var actions = secondBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains("Obtaining action of specified reference build");
    }

    @Test
    void testWhenReferenceBuildIsFailed() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("parasoft_coverage.xml", "parasoft_coverage_no_data.xml");
        setPipelineScript(job,
                "recordParasoftCoverage coverageQualityGates: [" + COVERAGE_QUALITY_GATE_SCRIPT + "], " + " referenceBuild: '1', pattern: '" + "parasoft_coverage_no_data.xml" + "', sourceCodeEncoding: '" + SOURCECODE_ENCODING + "'");
        buildWithResult(job, Result.FAILURE);
        setPipelineScript(job,
                "recordParasoftCoverage coverageQualityGates: [" + COVERAGE_QUALITY_GATE_SCRIPT + "], " + " referenceBuild: '1', pattern: '" + COVERAGE_FILE + "', sourceCodeEncoding: '" + SOURCECODE_ENCODING + "'");
        Run<?, ?> secondBuild = buildSuccessfully(job);
        var actions = secondBuild.getActions(CoverageBuildAction.class);
        var result = actions.get(0);
        assertThat(result.getLog().getInfoMessages().toString()).contains("The reference build 'test1 #1' is not successful or unstable");
    }
}
