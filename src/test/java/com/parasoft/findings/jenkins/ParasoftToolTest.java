
package com.parasoft.findings.jenkins;

import com.parasoft.findings.jenkins.tool.ParasoftTableModel;
import com.parasoft.findings.jenkins.tool.ParasoftTableModel.ParasoftTableRow;
import com.parasoft.findings.jenkins.tool.ParasoftTool;
import com.parasoft.findings.jenkins.tool.ParasoftTool.Descriptor;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool.ReportScanningToolDescriptor;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;
import org.jenkins.ui.symbol.SymbolRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParasoftToolTest {

    private static final String REPORT_NAME = "jtest_10.6.0_static.xml";
    private static final String TOOL_NAME = "Parasoft Findings";

    @Test
    void scanReportTest() throws IOException, InterruptedException {
        File tempDir = FileUtil.getTempDir();
        try {
            String localSettingsPath = new File("src/test/resources/settings")
                    .getAbsolutePath() + "/settings.properties";
            FreeStyleBuild freeStyleBuild = Mockito.mock(FreeStyleBuild.class);
            Mockito.when(freeStyleBuild.getRootDir()).thenReturn(tempDir);
            Mockito.when(freeStyleBuild.getEnvironment(TaskListener.NULL))
                    .thenReturn(new EnvVars());
            LogHandler logger = Mockito.mock(LogHandler.class);

            ParasoftTool underTest = Mockito.spy(new UnderTest());
            underTest.setLocalSettingsPath(localSettingsPath);

            Mockito.doReturn(new Descriptor()).when((ReportScanningTool)underTest).getDescriptor();

            Report report = underTest.scan(freeStyleBuild,
                    new FilePath(new File("src/test/resources/xml")),
                    StandardCharsets.UTF_8, logger);

            Set<Severity> severieties = report.getSeverities();
            assertEquals(17, report.getSize());
            assertTrue(severieties.contains(Severity.WARNING_HIGH));
            assertTrue(severieties.contains(Severity.WARNING_NORMAL));
            assertFalse(severieties.contains(Severity.WARNING_LOW));
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    @Test
    void labelProviderAndDescriptorTest() throws IOException, InterruptedException {
        File tempDir = FileUtil.getTempDir();
        try {
            String localSettingsPath = new File("src/test/resources/settings")
                    .getAbsolutePath() + "/settings.properties";
            FreeStyleBuild freeStyleBuild = Mockito.mock(FreeStyleBuild.class);
            Mockito.when(freeStyleBuild.getRootDir()).thenReturn(tempDir);
            LogHandler logger = Mockito.mock(LogHandler.class);
            Mockito.when(freeStyleBuild.getEnvironment(TaskListener.NULL))
                    .thenReturn(new EnvVars());

            ParasoftTool underTest = new UnderTest();
            underTest.setLocalSettingsPath(localSettingsPath);

            JenkinsFacade jenkinsFacade = Mockito.mock(JenkinsFacade.class);
            Mockito.when(jenkinsFacade.getDescriptorOrDie(ArgumentMatchers.any())).thenReturn(new Descriptor());
            underTest.setJenkinsFacade(jenkinsFacade);

            Report report = underTest.scan(freeStyleBuild,
                    new FilePath(new File("src/test/resources/xml")),
                    StandardCharsets.UTF_8, logger);

            ReportScanningToolDescriptor descriptor = new Descriptor();
            assertNotNull(descriptor);
            assertEquals("Parasoft Findings", descriptor.getName());
            assertEquals("https://www.parasoft.com/", descriptor.getUrl());
            assertEquals("**/report.xml", descriptor.getPattern());
            assertEquals("Parasoft Findings", descriptor.getDisplayName());
            assertEquals("parasoft-findings", descriptor.getId());

            StaticAnalysisLabelProvider labelProvider = descriptor.getLabelProvider();
            assertEquals("parasoft-findings", labelProvider.getId());
            assertEquals("/plugin/parasoft-findings/icons/parasofttest48.png", labelProvider.getLargeIconUrl());
            assertEquals("/plugin/parasoft-findings/icons/parasofttest24.png", labelProvider.getSmallIconUrl());

            Mockito.when(jenkinsFacade.getSymbol(ArgumentMatchers.any(SymbolRequest.class))).thenReturn("<svg>details-open-close-icon</svg>");
            ((ParasoftTool.LabelProvider) labelProvider).setJenkinsFacade(jenkinsFacade);

            ParasoftTableModel model = (ParasoftTableModel)labelProvider.getIssuesModel(freeStyleBuild, "parasoft-findings", report);
            assertEquals(9, model.getColumns().size());
            assertEquals("issues", model.getId());
            List<Object> rows = model.getRows();
            assertEquals(17, rows.size());
            for (Object object : rows) {
                assertInstanceOf(ParasoftTableRow.class, object);
            }
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    private static class UnderTest
            extends ParasoftTool {
        @Override
        public String getActualPattern() {
            return REPORT_NAME;
        }

        @Override
        public String getPattern() {
            return REPORT_NAME;
        }

        @Override
        public String getActualId() {
            return TOOL_NAME;
        }

        @Override
        public String getActualName() {
            return TOOL_NAME;
        }
    }
}
