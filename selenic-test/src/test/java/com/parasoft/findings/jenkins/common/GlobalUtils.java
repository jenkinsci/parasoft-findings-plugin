package com.parasoft.findings.jenkins.common;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.pages.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;

public class GlobalUtils {
    public static void createJob(WebDriver driver, String jobName) {
        switchToPage(driver, Properties.BASE_URL);
        DashboardPage dashboardPage = new DashboardPage(driver);
        dashboardPage.clickNewItemLink();

        NewItemPage newItemPage = new NewItemPage(driver);
        newItemPage.setItemNameInput(jobName);
        newItemPage.clickFreestyleProjectText();
        newItemPage.clickOKButton();
    }

    public static void switchToPage(WebDriver driver, String pageName) {
        driver.get(pageName);
    }

    public static void configureTestProject(WebDriver driver, String projectGitUrl, String command) {
        ConfigPage configPage = new ConfigPage(driver);
        configPage.clickSourceCodeManagementButton();
        configPage.clickGitRadio();
        configPage.setRepositoryUrlInput(projectGitUrl);
        configPage.clickBuildStepsButton();
        configPage.clickAddBuildStepButton();
        configPage.clickExecuteWindowsBatchCommandLink();
        configPage.setCommandInput(command);
        configPage.clickPostBuildActionsButton();
        configPage.clickAddPostBuildActionButton();
        configPage.clickRecordCompilerWarningsAndStaticAnalysisResultsLink();
        configPage.clickPostBuildActionsButton();
        configPage.selectToolDropdown(Properties.PARASOFT_FINDINGS_PLUGIN_DROPDOWN_OPTION);
        configPage.setLocalSettingsPathInput(Properties.PARASOFT_FINDINGS_PLUGIN_SETTINGS_TEXT);
        configPage.clickApplyButton();
    }

    public static JobDetailPage buildProject(WebDriver driver, String projectName) {
        switchToPage(driver, Properties.BASE_URL + "/job/" + projectName + "/");
        JobDetailPage jobDetailPage = new JobDetailPage(driver);
        jobDetailPage.clickBuildNowLink();
        jobDetailPage.waitBuildFinished(driver);
        return jobDetailPage;
    }

    public static void deleteProject(WebDriver driver, String projectName) {
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        String currentUrl = driver.getCurrentUrl();
        switchToPage(driver, Properties.BASE_URL + "/job/" + projectName + "/");

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
        assertTrue(projectDeleteAlert.getText().contains(projectName));
        projectDeleteAlert.accept();

        // Make sure the project is deleted
        assertEquals(driver.getCurrentUrl(), Properties.BASE_URL);
        Exception exception = null;
        try {
            driver.findElement(By.linkText(projectName));
        } catch (Exception e){
            exception = e;
        } finally {
            assertNotNull(exception);
        }
    }
}
