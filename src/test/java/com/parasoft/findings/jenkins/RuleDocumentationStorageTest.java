package com.parasoft.findings.jenkins;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import com.parasoft.findings.jenkins.internal.rules.JenkinsRulesUtil;
import com.parasoft.findings.jenkins.internal.rules.RuleDocumentationStorage;

import hudson.FilePath;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.fail;

class RuleDocumentationStorageTest {

    private static Properties settingsProperties;

    @BeforeAll
    static void setUp() {
        URI workspaceDir = new File("src/test/resources/settings").toURI();
        settingsProperties = JenkinsRulesUtil.loadSettings(new FilePath(new File(workspaceDir)),
                "settings.properties");
    }

    @Test
    @EnabledIf(value = "hasDtpUrlProperty",
            disabledReason = "No dtp.url property in src/test/resources/settings/settings.properties")
    void dtpRuleDownloadTest() throws IOException {
        File tempDir = FileUtil.getTempDir();
        try {
            RuleDocumentationStorage underTest = new RuleDocumentationStorage(tempDir, settingsProperties);
            checkIfRuleExist(underTest, tempDir, "OOP.MUCOP");
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    @Test
    void localRuleTest_getRuleDocFromLocalDir() throws IOException {
        testStoreRuleDocFileFromLocalFile("src/test/resources/rule");
    }

    @Test
    void localRuleTest_getRuleDocFromZipFile() throws IOException {
        testStoreRuleDocFileFromLocalFile("src/test/resources/rule/doc.zip");
    }

    private void testStoreRuleDocFileFromLocalFile(String ruleDocLocation) throws IOException {
        File tempDir = FileUtil.getTempDir();
        try {
            URL resource = new File(ruleDocLocation).toURI().toURL();
            Properties settings = new Properties();
            settings.put("report.rules", resource.getPath());
            RuleDocumentationStorage underTest = new RuleDocumentationStorage(tempDir, settings);
            checkIfRuleExist(underTest, tempDir, "APSC_DV.000160.SRD");
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

    private void checkIfRuleExist(RuleDocumentationStorage underTest, File tempDir, String ruleName) {
        underTest.storeRuleDoc("com.parasoft.jtest.standards.checkers.java", ruleName);
        File rule = new File(tempDir.getAbsolutePath() + "/parasoft-findings-rules/"
                + "com.parasoft.jtest.standards.checkers.java/" + ruleName + ".html");
        if (!rule.exists()) {
            fail();
        }
    }

    boolean hasDtpUrlProperty() {
        final String dtpUrl = settingsProperties.getProperty("dtp.url");
        return dtpUrl != null && !dtpUrl.trim().isEmpty();
    }
}
