package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class ConfigPage {
    @FindBy(xpath = "//*[@id='tasks']/div[2]/span/button")
    private WebElement sourceCodeManagementButton;

    @FindBy(xpath = "//*[@id='main-panel']/form/div[1]/div[6]/div[3]/div[1]/div/div/label['Git']")
    private WebElement git;

    @FindBy(name = "_.url")
    private WebElement urlField;

    @FindBy(xpath = "//*[@id='tasks']/div[5]/span/button/span['Build Steps']")
    private WebElement buildStepsButton;

    @FindBy(xpath = "//*[@id='main-panel']/form/div[1]/div[9]/div[2]/div/div/div[2]/span/span/button['Add build step']")
    private WebElement addBuildStepButton;

    @FindBy(linkText = "Execute Windows batch command")
    private WebElement executeWindowsBatchCommandLink;

    @FindBy(name = "command")
    private WebElement commandField;

    @FindBy(xpath = "//*[@id='tasks']/div[6]/span/button/span['Post-build Actions']")
    private WebElement postBuildActionsButton;

    @FindBy(xpath = "//*[@id='main-panel']/form/div[1]/div[10]/div[2]/div/div/div[2]/span/span/button['Add post-build action']")
    private WebElement addPostBuildActionButton;

    @FindBy(linkText = "Record compiler warnings and static analysis results")
    private WebElement recordCompilerWarningsAndStaticAnalysisResultsLink;

    @FindBy(css = "#tools .jenkins-select__input")
    private WebElement toolDropdown;

    @FindBy(name = "_.localSettingsPath")
    private WebElement localSettingsPathField;

    @FindBy(name = "Apply")
    private WebElement applyButton;

    private WebDriver driver;

    public ConfigPage(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    public void clickSourceCodeManagementButton() {
        ElementUtils.waitUntilVisible(driver, sourceCodeManagementButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, sourceCodeManagementButton);
    }

    public void clickGitRadio() {
        ElementUtils.waitUntilVisible(driver, git, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, git);
    }

    public void setUrlField(String text) {
        ElementUtils.waitUntilVisible(driver, urlField, Properties.WAIT_FOR_TIMEOUT).clear();
        urlField.sendKeys(text);
    }

    public void clickBuildStepsButton() {
        ElementUtils.waitUntilVisible(driver, buildStepsButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.scrollTo(addBuildStepButton,driver);
        ElementUtils.clickElementUseJs(driver, buildStepsButton);
    }

    public void clickAddBuildStepDropdown() {
        ElementUtils.waitUntilClickable(driver, addBuildStepButton, Properties.WAIT_FOR_TIMEOUT).click();
        ElementUtils.waitUntilVisible(driver, addBuildStepButton, Properties.WAIT_FOR_TIMEOUT);
    }

    public void clickExecuteWindowsBatchCommandLink() {
        ElementUtils.waitUntilVisible(driver, executeWindowsBatchCommandLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, executeWindowsBatchCommandLink);
    }

    public void setCommandField(String text) {
        ElementUtils.waitUntilVisible(driver, commandField, Properties.WAIT_FOR_TIMEOUT).clear();
        commandField.sendKeys(text);
    }

    public void clickPostBuildActionsButton() {
        ElementUtils.waitUntilVisible(driver, postBuildActionsButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, postBuildActionsButton);
    }

    public void clickAddPostBuildActionDropdown() {
        ElementUtils.waitUntilClickable(driver, addPostBuildActionButton, Properties.WAIT_FOR_TIMEOUT).click();
        ElementUtils.waitUntilVisible(driver, addPostBuildActionButton, Properties.WAIT_FOR_TIMEOUT);
    }

    public void clickRecordCompilerWarningsAndStaticAnalysisResultsLink() {
        ElementUtils.waitUntilVisible(driver, recordCompilerWarningsAndStaticAnalysisResultsLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, recordCompilerWarningsAndStaticAnalysisResultsLink);
    }

    public void selectToolDropdown(String text) {
        ElementUtils.waitUntilVisible(driver, toolDropdown, Properties.WAIT_FOR_TIMEOUT);
        clickPostBuildActionsButton();
        Select dropdown = new Select(toolDropdown);
        dropdown.selectByVisibleText(text);
    }

    public void setLocalSettingsPathField(String text) {
        ElementUtils.waitUntilVisible(driver, localSettingsPathField, Properties.WAIT_FOR_TIMEOUT).clear();
        localSettingsPathField.sendKeys(text);
    }

    public void clickApplyButton() {
        ElementUtils.waitUntilVisible(driver, applyButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilClickable(driver, applyButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, applyButton);
    }
}