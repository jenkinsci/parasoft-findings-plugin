package com.parasoft.findings.jenkins.coverage;

import hudson.model.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;

import org.jvnet.hudson.test.SingleFileSCM;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@WithJenkins
class ParasoftCoverageRecorderTest {

    private static final String success = "Finished: SUCCESS";
    private static final String failure = "Finished: FAILURE";
    private static final String usingDefaultPattern = "[Parasoft Coverage] Using default pattern '**/coverage.xml' for '' since specified pattern is empty";

    private JenkinsRule jenkinsRule;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @Test
    void testUsingSetPattern() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        parasoftCoverageRecorder.setPattern("**/coverage.xml");
        String log = runPlugin(parasoftCoverageRecorder, true);
        assertFalse(log.contains(usingDefaultPattern));
        assertTrue(log.contains(success));
    }

    @Test
    void testUsingDefaultPattern() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        String log = runPlugin(parasoftCoverageRecorder, true);
        assertTrue(log.contains(usingDefaultPattern));
        assertTrue(log.contains(success));
    }

    @Test
    void testEmptyCoverageFile() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.setScm(new SingleFileSCM("coverage.xml", ""));
        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile(), "UTF-8");
        assertTrue(log.contains("[Parasoft Coverage] [-ERROR-] Skipping file 'coverage.xml' because it's empty"));
        assertTrue(log.contains(success));
    }

    @Test
    void testNotFoundCoverageFile() throws Exception {
        ParasoftCoverageRecorder parasoftCoverageRecorder = new ParasoftCoverageRecorder();
        String log = runPlugin(parasoftCoverageRecorder, false);
        assertTrue(log.contains("[Parasoft Coverage] [-ERROR-] No files found for pattern '**/coverage.xml'. Configuration error?"));
        assertTrue(log.contains(success));
    }

    @Test
    void testNoWorkspace() {
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

    private String runPlugin(ParasoftCoverageRecorder parasoftCoverageRecorder, boolean setXML) throws Exception {
        parasoftCoverageRecorder.setSourceCodeEncoding("UTF-8");
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        if(setXML) {
            project.setScm(new SingleFileSCM("coverage.xml", getClass().getResource("/coverage.xml")));
        }
        project.getPublishersList().add(parasoftCoverageRecorder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        return FileUtils.readFileToString(build.getLogFile(), "UTF-8");
    }
}
