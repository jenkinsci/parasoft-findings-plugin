package com.parasoft.findings.jenkins.testcases;

import com.parasoft.findings.jenkins.pages.JobDetailPage;
import com.parasoft.findings.jenkins.pages.ParasoftWarningsPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import com.parasoft.findings.jenkins.common.GlobalUtils;
import com.parasoft.findings.jenkins.common.Properties;
import com.parasoft.findings.jenkins.common.WebDriverInitialization;

import static org.junit.jupiter.api.Assertions.*;

public class DottestProjectTests {
    private WebDriver driver;
    private final String projectName = Properties.DOTTEST_JOB_NAME;

    @BeforeEach
    public void beforeTest() {
        driver = WebDriverInitialization.init();
        driver.manage().window().maximize();
    }

    @AfterEach
    public void afterTest() {
        GlobalUtils.deleteProject(driver, projectName);
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testParasoftFindingsPlugin() {
        GlobalUtils.createJob(driver, projectName);
        GlobalUtils.configureTestProject(driver, Properties.DOTTEST_PROJECT_GIT_URL, Properties.DOTTEST_PROJECT_COMMAND);
        JobDetailPage jobDetailPage = GlobalUtils.buildProject(driver, projectName);
        jobDetailPage.clickParasoftWarningsLink();

        // Check test information in warnings page
        ParasoftWarningsPage parasoftWarningsPage = new ParasoftWarningsPage(driver);
        parasoftWarningsPage.clickNamespacesLink();
        assertTrue(parasoftWarningsPage.getNamespacesInfoText().contains(Properties.DOTTEST_NAMESPACES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickFilesLink();
        assertTrue(parasoftWarningsPage.getFileInfoText().contains(Properties.DOTTEST_FILES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickCategoriesLink();
        assertTrue(parasoftWarningsPage.getCategoryInfoText().contains(Properties.DOTTEST_CATEGORIES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickIssuesLink();
        assertTrue(parasoftWarningsPage.getIssuesInfoText().contains(Properties.DOTTEST_ISSUES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickTypesLink();
        assertTrue(parasoftWarningsPage.getTypeInfoText().contains(Properties.DOTTEST_TYPES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickRuleTypeLink("BD.EXCEPT.NR");
        assertEquals(parasoftWarningsPage.getRuleTitleText(), Properties.DOTTEST_RULE_TYPE_ASSERTATION);

        parasoftWarningsPage.clickIssuesLink();
        parasoftWarningsPage.clickOpenIconButton();
        assertFalse(parasoftWarningsPage.getRuleDetailsText().isEmpty());
    }
}
