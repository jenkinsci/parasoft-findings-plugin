package com.parasoft.findings.jenkins.coverage;

import hudson.model.*;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import org.jvnet.hudson.test.SingleFileSCM;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ParasoftCoverageRecorderTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testUsingSetPattern() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        parasoftCoverageRecorder.setPattern("**/coverage.xml");
        String log = runPlugin(parasoftCoverageRecorder, true);
        System.out.println(log);
        Assert.assertFalse(log.contains("[Parasoft Coverage] Using default pattern '**/coverage.xml' for '' since specified pattern is empty"));
        Assert.assertTrue(log.contains("Finished: SUCCESS"));
    }

    @Test
    public void testUsingDefaultPattern() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        String log = runPlugin(parasoftCoverageRecorder, true);
        Assert.assertTrue(log.contains("[Parasoft Coverage] Using default pattern '**/coverage.xml' for '' since specified pattern is empty"));
        Assert.assertTrue(log.contains("Finished: SUCCESS"));
    }

    @Test
    public void testEmptyCoverageFile() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.setScm(new SingleFileSCM("coverage.xml", ""));
        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile(), "UTF-8");
        Assert.assertTrue(log.contains("[Parasoft Coverage] [-ERROR-] Skipping file 'coverage.xml' because it's empty"));
        Assert.assertTrue(log.contains("Finished: FAILURE"));
    }

    @Test
    public void testNotFindCoverageFile() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        String log = runPlugin(parasoftCoverageRecorder, false);
        Assert.assertTrue(log.contains("[Parasoft Coverage] [-ERROR-] No files found for pattern '**/coverage.xml'. Configuration error?"));
        Assert.assertTrue(log.contains("Finished: FAILURE"));
    }

    @Test
    public void testNoWorkspace() {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        AbstractBuild<?, ?> build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getWorkspace()).thenReturn(null);
        String message = "";
        try {
            parasoftCoverageRecorder.perform(build, null, null);
        } catch (Exception e) {
            message = e.getMessage();
        }
        assertEquals("No workspace found for " + build, message);
    }

    private String runPlugin(ParasoftCoverageRecorder parasoftCoverageRecorder, boolean setXML) throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        if(setXML) {
            project.setScm(new SingleFileSCM("coverage.xml", getClass().getResource("/coverage.xml")));
        }
        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        return FileUtils.readFileToString(build.getLogFile(), "UTF-8");
    }
}
