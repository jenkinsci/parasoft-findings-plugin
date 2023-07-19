package com.parasoft.findings.jenkins.coverage;

import hudson.Launcher;
import hudson.model.*;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import org.jvnet.hudson.test.SingleFileSCM;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class ParasoftCoverageRecorderTest {
    Launcher launcher;
    BuildListener listener;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testNotFindCoverageFile() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();

        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile(), "UTF-8");
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
            parasoftCoverageRecorder.perform(build, launcher, listener);
        } catch (Exception e) {
            message = e.getMessage();
        }
        assertEquals("No workspace found for " + build, message);
    }

    @Test
    public void testUsingSetPattern() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.setScm(new SingleFileSCM("coverage.xml", getClass().getResource("/coverage.xml")));
        parasoftCoverageRecorder.setPattern("**/coverage.xml");
        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile(), "UTF-8");
        Assert.assertFalse(log.contains("[Parasoft Coverage] Using default pattern '**/coverage.xml' for '' since specified pattern is empty"));
        Assert.assertTrue(log.contains("Finished: SUCCESS"));
    }

    @Test
    public void testUsingDefaultPattern() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.setScm(new SingleFileSCM("coverage.xml", getClass().getResource("/coverage.xml")));
        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile(), "UTF-8");
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
        Assert.assertTrue(log.contains("[Parasoft Coverage] [-ERROR-] Errors while recording Parasoft code coverage:"));
        Assert.assertTrue(log.contains("Finished: FAILURE"));
    }
}
