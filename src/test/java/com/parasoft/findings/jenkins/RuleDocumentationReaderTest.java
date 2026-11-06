package com.parasoft.findings.jenkins;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

import com.parasoft.findings.jenkins.internal.rules.RuleDocumentationReader;

import org.junit.jupiter.api.Test;
import com.parasoft.findings.jenkins.internal.rules.RuleDocumentationStorage;

class RuleDocumentationReaderTest {

    private String ruleName = "APSC_DV.000160.SRD";
    private String analyzer = "com.parasoft.jtest.standards.checkers.java";

    @Test
    void documentationReaderTest() throws IOException {
        File tempDir = FileUtil.getTempDir();
        try {
            URL resource = new File("src/test/resources/rule").toURI().toURL();
            Properties settings = new Properties();
            settings.put("report.rules", resource.getPath());
            RuleDocumentationStorage helper = new RuleDocumentationStorage(tempDir, settings);
            helper.storeRuleDoc(analyzer, ruleName);
            File rule = new File(tempDir.getAbsolutePath() + "/parasoft-findings-rules/"
                    + analyzer + "/" + ruleName + ".html");
            if (!rule.exists()) {
                fail();
            }
            RuleDocumentationReader underTest = new RuleDocumentationReader(tempDir);
            String ruleDoc = underTest.getRuleDoc(analyzer, ruleName);
            assertNotNull(ruleDoc);
            String ruleText = null;
            try (Scanner scanner = new Scanner( new File( resource.getPath() + ruleName + ".html"), StandardCharsets.UTF_8)) {
                ruleText = scanner.useDelimiter("\\A").next();
            }
            assertEquals(ruleText, ruleDoc);
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

}
