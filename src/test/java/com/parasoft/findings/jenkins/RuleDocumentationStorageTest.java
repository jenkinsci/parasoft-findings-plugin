package com.parasoft.findings.jenkins;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;

import com.parasoft.findings.jenkins.internal.rules.JenkinsRulesUtil;
import com.parasoft.findings.jenkins.internal.rules.RuleDocumentationStorage;

import hudson.FilePath;

public class RuleDocumentationStorageTest
{
    @Test
    public void dtpRuleDownloadTest() throws IOException
    {
        File tempDir = FileUtil.getTempDir();
        try {
            URI workspaceDir = null;
            workspaceDir = new File("src/test/resources/settings").toURI();
            Properties settings = JenkinsRulesUtil.loadSettings(new FilePath(new File(workspaceDir)), "settings.properties");
            RuleDocumentationStorage underTest = new RuleDocumentationStorage(tempDir, settings);
            checkIfRuleExist(underTest, tempDir, "OOP.MUCOP");
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    @Test
    public void localRuleTest() throws IOException
    {
        File tempDir = FileUtil.getTempDir();
        try {
            URL resource = null;
            try {
                resource = new File("src/test/resources/rule").toURI().toURL();
            } catch (MalformedURLException e) {
                fail();
            }
            Properties settings = new Properties();
            settings.put("report.rules", resource.getPath());
            RuleDocumentationStorage underTest = new RuleDocumentationStorage(tempDir, settings);
            checkIfRuleExist(underTest, tempDir, "APSC_DV.000160.SRD");
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    private void checkIfRuleExist(RuleDocumentationStorage underTest, File tempDir, String ruleName)
    {
        underTest.storeRuleDoc("com.parasoft.jtest.standards.checkers.java", ruleName);
        File rule = new File(tempDir.getAbsolutePath() + "/parasoft-findings-rules/"
                + "com.parasoft.jtest.standards.checkers.java/" + ruleName + ".html");
        if (!rule.exists()) {
            fail();
        }
    }
}
