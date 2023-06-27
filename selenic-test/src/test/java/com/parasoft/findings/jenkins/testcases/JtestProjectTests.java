package com.parasoft.findings.jenkins.testcases;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import com.parasoft.findings.jenkins.common.GlobalUtils;
import com.parasoft.findings.jenkins.common.ParasoftWarningsInformation;
import com.parasoft.findings.jenkins.common.Properties;
import com.parasoft.findings.jenkins.common.WebDriverInitialization;

public class JtestProjectTests {
    private WebDriver driver;
    private ParasoftWarningsInformation parasoftWarningsInformation;

    @BeforeEach
    public void beforeTest() {
        driver = WebDriverInitialization.init();
        driver.manage().window().maximize();

        parasoftWarningsInformation = new ParasoftWarningsInformation(Properties.JTEST_JOB_NAME,
                Properties.JTEST_PACKAGES_TOTAL_NUMBER_ASSERTATION, Properties.JTEST_FILES_TOTAL_NUMBER_ASSERTATION,
                Properties.JTEST_TYPES_TOTAL_NUMBER_ASSERTATION, Properties.JTEST_ISSUES_INFO_ASSERTATION);
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
        GlobalUtils.configureTestProject(driver, Properties.JTEST_PROJECT_GIT_URL, Properties.JTEST_PROJECT_COMMAND);
        GlobalUtils.buildProject(driver, parasoftWarningsInformation.getProjectName());
        GlobalUtils.checkTestInfo(driver, "jtest", parasoftWarningsInformation);
    }
}
