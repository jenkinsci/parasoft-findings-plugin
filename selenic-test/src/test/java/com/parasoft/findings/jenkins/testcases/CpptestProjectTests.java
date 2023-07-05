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

public class CpptestProjectTests {
    private WebDriver driver;
    private final String projectName = Properties.CPPTEST_JOB_NAME;

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
        GlobalUtils.configureTestProject(driver, Properties.CPPTEST_PROJECT_GIT_URL, Properties.CPPTEST_PROJECT_COMMAND);
        JobDetailPage jobDetailPage = GlobalUtils.buildProject(driver, projectName);
        jobDetailPage.clickParasoftWarningsLink();

        // Check test information in warnings page
        ParasoftWarningsPage parasoftWarningsPage = new ParasoftWarningsPage(driver);
        parasoftWarningsPage.clickModulesLink();
        assertTrue(parasoftWarningsPage.getModulesInfoText().contains(Properties.CPPTEST_MODULES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickFoldersLink();
        assertTrue(parasoftWarningsPage.getFolderInfoText().contains(Properties.CPPTEST_FOLDERS_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickCategoriesLink();
        assertTrue(parasoftWarningsPage.getCategoryInfoText().contains(Properties.CPPTEST_CATEGORIES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickFilesLink();
        assertTrue(parasoftWarningsPage.getFileInfoText().contains(Properties.CPPTEST_FILES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickIssuesLink();
        assertTrue(parasoftWarningsPage.getIssuesInfoText().contains(Properties.CPPTEST_ISSUES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickTypesLink();
        assertTrue(parasoftWarningsPage.getTypeInfoText().contains(Properties.CPPTEST_TYPES_ENTRIES_ASSERTATION));

        parasoftWarningsPage.clickRuleTypeLink("APSC_DV-003110-a");
        assertEquals(parasoftWarningsPage.getRuleTitleText(), Properties.CPPTEST_RULE_TYPE_ASSERTATION);

        parasoftWarningsPage.clickIssuesLink();
        parasoftWarningsPage.clickOpenIconButton();
        WebElement ruleDetailsText = driver.findElement(By.xpath("//*[@id='issues']/tbody/tr[2]/td/strong[1]"));
        assertFalse(parasoftWarningsPage.getRuleDetailsText(ruleDetailsText).isEmpty());
        assertTrue(parasoftWarningsPage.getRuleDetailsText(ruleDetailsText).contains("APSC_DV-003110-a"));
    }
}
