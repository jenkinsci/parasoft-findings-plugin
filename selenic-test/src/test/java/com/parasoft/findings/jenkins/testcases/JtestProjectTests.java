package com.parasoft.findings.jenkins.testcases;

import com.parasoft.findings.jenkins.pages.JobDetailPage;
import com.parasoft.findings.jenkins.pages.ParasoftWarningsPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.parasoft.findings.jenkins.common.GlobalUtils;
import com.parasoft.findings.jenkins.common.Properties;
import com.parasoft.findings.jenkins.common.WebDriverInitialization;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class JtestProjectTests {
    private WebDriver driver;
    private final String projectName = Properties.JTEST_JOB_NAME;

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
        GlobalUtils.configureTestProject(driver, Properties.JTEST_PROJECT_GIT_URL, Properties.JTEST_PROJECT_COMMAND);
        JobDetailPage jobDetailPage = GlobalUtils.buildProject(driver, projectName);
        jobDetailPage.clickParasoftWarningsLink();

        // Check test information in warnings page
        ParasoftWarningsPage parasoftWarningsPage = new ParasoftWarningsPage(driver);
        parasoftWarningsPage.clickPackagesLink();
        assertTrue(parasoftWarningsPage.getPackageInfoText().contains(Properties.JTEST_PACKAGES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickFilesLink();
        assertTrue(parasoftWarningsPage.getFileInfoText().contains(Properties.JTEST_FILES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickCategoriesLink();
        assertTrue(parasoftWarningsPage.getCategoryInfoText().contains(Properties.JTEST_CATEGORIES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickIssuesLink();
        assertTrue(parasoftWarningsPage.getIssuesInfoText().contains(Properties.JTEST_ISSUES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickTypesLink();
        assertTrue(parasoftWarningsPage.getTypeInfoText().contains(Properties.JTEST_TYPES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickRuleTypeLink("APSC_DV.001460.SIO");
        assertEquals(parasoftWarningsPage.getRuleTitleText(), Properties.JTEST_RULE_TYPE_ASSERTATION);

        parasoftWarningsPage.clickIssuesLink();
        parasoftWarningsPage.clickOpenIconButton();
        WebElement ruleDetailsText = driver.findElement(By.xpath("//*[@id='issues']//tr[2]//strong"));
        assertFalse(parasoftWarningsPage.getRuleDetailsText(ruleDetailsText).isEmpty());
    }
}
