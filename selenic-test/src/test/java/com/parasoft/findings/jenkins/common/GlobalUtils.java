package com.parasoft.findings.jenkins.common;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.pages.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;

public class GlobalUtils {
    public static void createJob(WebDriver driver, String jobName) {
        switchToPage(driver, Properties.DASHBOARD_PAGE);
        DashboardPage dashboardPage = new DashboardPage(driver);
        dashboardPage.clickNewItemLink();

        NewItemPage newItemPage = new NewItemPage(driver);
        newItemPage.setItemNameField(jobName);
        newItemPage.clickFreestyleProjectElement();
        newItemPage.clickOKButton();
    }

    public static void switchToPage(WebDriver driver, String pageName) {
        driver.get(pageName);
    }

    public static void configureTestProject(WebDriver driver, String projectGitUrl, String command) {
        ConfigPage configPage = new ConfigPage(driver);
        configPage.clickSourceCodeManagementButton();
        configPage.clickGitRadio();
        configPage.setUrlField(projectGitUrl);
        configPage.clickBuildStepsButton();
        configPage.clickAddBuildStepDropdown();
        configPage.clickExecuteWindowsBatchCommandLink();
        configPage.setCommandField(command);
        configPage.clickPostBuildActionsButton();
        configPage.clickAddPostBuildActionDropdown();
        configPage.clickRecordCompilerWarningsAndStaticAnalysisResultsLink();
        configPage.clickPostBuildActionsButton();
        configPage.selectToolDropdown(Properties.PARASOFT_FINDINGS_PLUGIN_DROPDOWN_OPTION);
        configPage.setLocalSettingsPathField(Properties.PARASOFT_FINDINGS_PLUGIN_SETTINGS_TEXT);
        configPage.clickApplyButton();
    }

    public static void buildProject(WebDriver driver, String projectName) {
        switchToPage(driver, Properties.DASHBOARD_PAGE + "/job/" + projectName + "/");
        JobDetailPage jobDetailPage = new JobDetailPage(driver);
        jobDetailPage.clickBuildNowLink();
        jobDetailPage.waitBuildFinished(driver);
        jobDetailPage.clickParasoftWarningsLink();
    }

    public static void deleteProject(WebDriver driver, String projectName) {
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        String currentUrl = driver.getCurrentUrl();
        switchToPage(driver, Properties.DASHBOARD_PAGE + "/job/" + projectName + "/");

        // If failed in config page, there is a alert when return to other page
        if(currentUrl.contains("/configure")) {
            Alert configurationAlert = driver.switchTo().alert();
            wait.until(alertIsPresent());
            configurationAlert.accept();
        }

        JobDetailPage jobDetailPage = new JobDetailPage(driver);
        jobDetailPage.clickDeleteProjectLink();

        wait.until(alertIsPresent());
        Alert projectDeleteAlert = driver.switchTo().alert();
        assertEquals("Delete the Project ‘" + projectName + "’?", projectDeleteAlert.getText());
        projectDeleteAlert.accept();
    }
}
