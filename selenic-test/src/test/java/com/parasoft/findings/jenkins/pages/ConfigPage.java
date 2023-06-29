package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class ConfigPage {
    @FindBy(css = "button[data-section-id='source-code-management']")
    private WebElement sourceCodeManagementButton;

    @FindBy(xpath = "//label[text()='Git']")
    private WebElement gitRadioLabel;

    @FindBy(name = "_.url")
    private WebElement repositoryUrlInput;

    @FindBy(css = "button[data-section-id='build-steps']")
    private WebElement buildStepsButton;

    @FindBy(xpath = "//button[text()='Add build step']")
    private WebElement addBuildStepButton;

    @FindBy(linkText = "Execute Windows batch command")
    private WebElement executeWindowsBatchCommandLink;

    @FindBy(name = "command")
    private WebElement commandTextarea;

    @FindBy(css = "button[data-section-id='post-build-actions']")
    private WebElement postBuildActionsButton;

    @FindBy(xpath = "//button[text()='Add post-build action']")
    private WebElement addPostBuildActionButton;

    @FindBy(linkText = "Record compiler warnings and static analysis results")
    private WebElement recordCompilerWarningsAndStaticAnalysisResultsLink;

    @FindBy(css = "#tools .jenkins-select__input")
    private WebElement toolDropdown;

    @FindBy(name = "_.localSettingsPath")
    private WebElement localSettingsPathInput;

    @FindBy(name = "Apply")
    private WebElement applyButton;

    private final WebDriver driver;

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
        ElementUtils.waitUntilVisible(driver, gitRadioLabel, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, gitRadioLabel);
    }

    public void setRepositoryUrlInput(String text) {
        ElementUtils.waitUntilVisible(driver, repositoryUrlInput, Properties.WAIT_FOR_TIMEOUT).clear();
        repositoryUrlInput.sendKeys(text);
    }

    public void clickBuildStepsButton() {
        ElementUtils.waitUntilVisible(driver, buildStepsButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.scrollTo(addBuildStepButton,driver);
        ElementUtils.clickElementUseJs(driver, buildStepsButton);
    }

    public void clickAddBuildStepButton() {
        ElementUtils.waitUntilClickable(driver, addBuildStepButton, Properties.WAIT_FOR_TIMEOUT).click();
        ElementUtils.waitUntilVisible(driver, addBuildStepButton, Properties.WAIT_FOR_TIMEOUT);
    }

    public void clickExecuteWindowsBatchCommandLink() {
        ElementUtils.waitUntilVisible(driver, executeWindowsBatchCommandLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, executeWindowsBatchCommandLink);
    }

    public void setCommandInput(String text) {
        ElementUtils.waitUntilVisible(driver, commandTextarea, Properties.WAIT_FOR_TIMEOUT).clear();
        commandTextarea.sendKeys(text);
    }

    public void clickPostBuildActionsButton() {
        ElementUtils.waitUntilVisible(driver, postBuildActionsButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, postBuildActionsButton);
    }

    public void clickAddPostBuildActionButton() {
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

    public void setLocalSettingsPathInput(String text) {
        ElementUtils.waitUntilVisible(driver, localSettingsPathInput, Properties.WAIT_FOR_TIMEOUT).clear();
        localSettingsPathInput.sendKeys(text);
    }

    public void clickApplyButton() {
        ElementUtils.waitUntilVisible(driver, applyButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilClickable(driver, applyButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, applyButton);
    }
}