
package com.parasoft.xtest.reports.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.parasoft.xtest.common.io.FileUtil;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.reports.jenkins.internal.services.JenkinsServicesProvider;
import com.parasoft.xtest.reports.jenkins.tool.ParasoftTableModel;
import com.parasoft.xtest.reports.jenkins.tool.ParasoftTableModel.ParasoftTableRow;
import com.parasoft.xtest.reports.jenkins.tool.ParasoftTool;
import com.parasoft.xtest.reports.jenkins.tool.ParasoftTool.Descriptor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool.ReportScanningToolDescriptor;
import io.jenkins.plugins.analysis.core.util.LogHandler;

public class ParasoftToolTest
{
    private static final String REPORT_NAME = "jtest_10.6.0_static.xml";
    private static final String TOOL_NAME = "Parasoft Findings";

    @BeforeClass
    public static void setUp()
    {
        JenkinsServicesProvider.init();
    }

    @Test
    public void scanReportTest()
        throws IOException, InterruptedException
    {
        File tempDir = FileUtil.getTempDir(new RawServiceContext());
        try {
            String localSettingsPath = new File("src/test/resources/settings")
                    .getAbsolutePath() + "/settings.properties";
            FreeStyleBuild freeStyleBuild = Mockito.mock(FreeStyleBuild.class);
            Mockito.when(freeStyleBuild.getRootDir()).thenReturn(tempDir);
            Mockito.when(freeStyleBuild.getEnvironment(TaskListener.NULL))
            .thenReturn(new EnvVars());
            LogHandler logger = Mockito.mock(LogHandler.class);

            ParasoftTool underTest = new UnderTest();
            underTest.setLocalSettingsPath(localSettingsPath);

            Report report = underTest.scan(freeStyleBuild,
                new FilePath(new File("src/test/resources/xml")),
                Charset.forName("UTF-8"), logger);

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
    public void labelProviderAndDescriptorTest() throws IOException, InterruptedException
    {
        File tempDir = FileUtil.getTempDir(new RawServiceContext());
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

            Report report = underTest.scan(freeStyleBuild,
                new FilePath(new File("src/test/resources/xml")),
                Charset.forName("UTF-8"), logger);

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

            ParasoftTableModel model = (ParasoftTableModel)labelProvider.getIssuesModel(freeStyleBuild, "parasoft-findings", report);
            assertEquals(9, model.getColumns().size());
            assertEquals("issues", model.getId());
            List<Object> rows = model.getRows();
            assertEquals(17, rows.size());
            for (Object object : rows) {
                assertTrue(object instanceof ParasoftTableRow);
            }
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    @SuppressWarnings("serial")
    private class UnderTest
        extends ParasoftTool
    {
        @Override
        public String getActualPattern()
        {
            return REPORT_NAME;
        }

        @Override
        public String getPattern()
        {
            return REPORT_NAME;
        }

        @Override
        public String getActualId()
        {
            return TOOL_NAME;
        }

        @Override
        public String getActualName()
        {
            return TOOL_NAME;
        }
    }
}
