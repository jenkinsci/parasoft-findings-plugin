package com.parasoft.findings.jenkins.testcases;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import com.parasoft.findings.jenkins.common.GlobalUtils;
import com.parasoft.findings.jenkins.common.ParasoftWarningsInformation;
import com.parasoft.findings.jenkins.common.Properties;
import com.parasoft.findings.jenkins.common.WebDriverInitialization;

public class DottestProjectTests {
    private WebDriver driver;
    private ParasoftWarningsInformation parasoftWarningsInformation;

    @BeforeEach
    public void beforeTest() {
        driver = WebDriverInitialization.init();
        driver.manage().window().maximize();

        parasoftWarningsInformation = new ParasoftWarningsInformation(Properties.DOTTEST_JOB_NAME,
                Properties.DOTTEST_PACKAGES_TOTAL_NUMBER_ASSERTATION, Properties.DOTTEST_FILES_TOTAL_NUMBER_ASSERTATION,
                Properties.DOTTEST_TYPES_TOTAL_NUMBER_ASSERTATION, Properties.DOTTEST_ISSUES_INFO_ASSERTATION);
    }

    @AfterEach
    public void afterTest() {
        GlobalUtils.deleteProject(driver, parasoftWarningsInformation.getProjectName());
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testParasoftFindingsPlugin() {
        GlobalUtils.createJob(driver, parasoftWarningsInformation.getProjectName());
        GlobalUtils.configureTestProject(driver, Properties.DOTTEST_PROJECT_GIT_URL, Properties.DOTTEST_PROJECT_COMMAND);
        GlobalUtils.buildProject(driver, parasoftWarningsInformation.getProjectName());
        GlobalUtils.checkTestInfo(driver, "dottest", parasoftWarningsInformation);
    }
}
