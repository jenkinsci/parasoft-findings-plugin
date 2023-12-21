package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.source.SourceCodePainter;
import com.parasoft.findings.jenkins.coverage.model.Node;
import com.parasoft.findings.jenkins.util.FilteredLogChain;
import edu.hm.hafner.util.FilteredLog;
import hudson.FilePath;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.QualityGate;
import io.jenkins.plugins.util.StageResultHandler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.util.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CoverageReporterTest extends AbstractCoverageTest {
    @Test
    public void testPublishAction_invalid_reference_build() {
        CoverageBuildAction action = runPublishAction(new ArrayList<>(), "#1");
        assertThat(action).isNotNull();
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo(Messages.Reference_Build_Warning_Message_NO_SPECIFIED_REF_BUILD("#1", "test_project"));
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
        Run<?, ?> build = mock(Run.class);
        doReturn(new File("fake/workspace/path")).when(build).getRootDir();
        Job<?,?> job = mock(Job.class);
        doReturn("test_project").when(job).getFullName();
        doReturn(job).when(build).getParent();
        MockedConstruction<SourceCodePainter> sourceCodePainterMockedConstruction = mockConstruction(SourceCodePainter.class);
        MockedConstruction<CoverageXmlStream> coverageXmlStreamMockedConstruction = mockConstruction(CoverageXmlStream.class);
        FilteredLogChain mockedLogChain = mock(FilteredLogChain.class);
        when(mockedLogChain.getLogHandler()).thenReturn(mock(LogHandler.class));
        when(mockedLogChain.addNewFilteredLog(anyString())).thenReturn(mock(FilteredLog.class));

        try {
            action = reporter.publishAction("parasoft-coverage", "symbol-footsteps-outline plugin-ionicons-api",
                    node, build, new FilePath(new File("com/parasoft/findings/jenkins/coverage/api/metrics/steps/test_project")),
                    TaskListener.NULL, "", configRefBuild, coverageQualityGate, "UTF-8", mock(StageResultHandler.class),
                    mockedLogChain
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(coverageXmlStreamMockedConstruction.constructed().size()).isEqualTo(1);
        assertThat(sourceCodePainterMockedConstruction.constructed().size()).isEqualTo(1);
        sourceCodePainterMockedConstruction.close();
        coverageXmlStreamMockedConstruction.close();
        verify(mockedLogChain).getLogHandler();
        verify(mockedLogChain).addNewFilteredLog(anyString());

        return action;
    }
}
