package com.parasoft.xtest.reports.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import com.parasoft.xtest.common.io.FileUtil;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.reports.jenkins.internal.rules.RuleDocumentationReader;
import com.parasoft.xtest.reports.jenkins.internal.rules.RuleDocumentationStorage;
import com.parasoft.xtest.reports.jenkins.internal.services.JenkinsServicesProvider;

public class RuleDocumentationReaderTest
{
    private String ruleName = "APSC_DV.000160.SRD";
    private String analyzer = "com.parasoft.jtest.standards.checkers.java";

    @BeforeClass
    public static void setUp()
    {
        JenkinsServicesProvider.init();
    }

    @Test
    public void documentationReaderTest() throws IOException
    {
        File tempDir = FileUtil.getTempDir(new RawServiceContext());
        try {
            URL resource = null;
            try {
                resource = new File("src/test/resources/rule").toURI().toURL();
            } catch (MalformedURLException e) {
                fail();
            }
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
            try (Scanner scanner = new Scanner( new File( resource.getPath() + ruleName + ".html"), "UTF-8" )) {
                ruleText = scanner.useDelimiter("\\A").next();
            }
            assertEquals(ruleText, ruleDoc);
        } finally {
            FileUtil.recursiveDelete(tempDir);
        }
    }

}
