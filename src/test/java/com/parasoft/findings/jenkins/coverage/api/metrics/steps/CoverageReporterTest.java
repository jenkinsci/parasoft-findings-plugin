package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.model.Node;
import hudson.FilePath;
import hudson.model.*;
import io.jenkins.plugins.util.QualityGate;
import io.jenkins.plugins.util.StageResultHandler;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.util.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CoverageReporterTest extends AbstractCoverageTest {
    private final Run<?, ?> build = mock(Run.class);

    @Test
    public void testPublishAction_invalid_reference_build() {
        CoverageBuildAction action = runPublishAction(new ArrayList<>(), "#1");
        assertThat(action).isNotNull();
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo(Messages.Reference_Build_Warning_Message_NO_SPECIFIED_REF_BUILD("#1"));
    }

    @Test
    public void testPublishAction_QualityGate_successful() {
        List<CoverageQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(new CoverageQualityGate(40, Baseline.PROJECT, QualityGate.QualityGateCriticality.UNSTABLE));

        CoverageBuildAction action = runPublishAction(qualityGates, "");
        assertThat(action).isNotNull();
        assertThat(action.getQualityGateResult()).isSuccessful();
    }

    @Test
    public void testPublishAction_QualityGate_failed() {
        List<CoverageQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(new CoverageQualityGate(100, Baseline.PROJECT, QualityGate.QualityGateCriticality.UNSTABLE));

        CoverageBuildAction action = runPublishAction(qualityGates, "");
        assertThat(action).isNotNull();
        assertThat(action.getQualityGateResult()).isNotSuccessful();
    }

    private CoverageBuildAction runPublishAction(List<CoverageQualityGate> coverageQualityGate, String configRefBuild) {
        Node node = readCoberturaResult("cobertura-codingstyle.xml");
        CoverageReporter reporter = new CoverageReporter();
        CoverageBuildAction action = null;

        doReturn(new File("com/parasoft/findings/jenkins/coverage/api/metrics/steps/test_project")).when(build).getRootDir();

        try {
            action = reporter.publishAction("parasoft-coverage", "symbol-footsteps-outline plugin-ionicons-api",
                    node, build, new FilePath(new File("com/parasoft/findings/jenkins/coverage/api/metrics/steps/test_project")),
                    TaskListener.NULL, configRefBuild, coverageQualityGate, "UTF-8", mock(StageResultHandler.class));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return action;
    }
}
