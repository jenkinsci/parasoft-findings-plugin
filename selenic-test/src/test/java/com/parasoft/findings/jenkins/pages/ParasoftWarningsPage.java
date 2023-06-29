package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class ParasoftWarningsPage {
    @FindBy(id = "moduleName_info")
    private WebElement moduleInfoText;

    @FindBy(id = "folder_info")
    private WebElement folderInfoText;

    @FindBy(id = "packageName_info")
    private WebElement packageInfoText;

    @FindBy(id = "fileName_info")
    private WebElement fileInfoText;

    @FindBy(id = "category_info")
    private WebElement categoryInfoText;

    @FindBy(id = "type_info")
    private WebElement typeInfoText;

    @FindBy(id = "issues_info")
    private WebElement issuesInfoText;

    @FindBy(linkText = "Modules")
    private WebElement modulesLink;

    @FindBy(linkText = "Namespaces")
    private WebElement namespacesLink;

    @FindBy(linkText = "Folders")
    private WebElement foldersLink;

    @FindBy(linkText = "Packages")
    private WebElement packagesLink;

    @FindBy(linkText = "Files")
    private WebElement filesLink;

    @FindBy(linkText = "Categories")
    private WebElement categoriesLink;

    @FindBy(linkText = "Types")
    private WebElement typesLink;

    @FindBy(linkText = "Issues")
    private WebElement issuesLink;

    @FindBy(id = "issues_paginate")
    private WebElement issuesPaginate;

    @FindBy(xpath = "//*[@id='issues']//tr[1]/td[1]/div")
    private WebElement openIcon;

    @FindBy(xpath = "//*[@id='issues']//tr[2]//strong")
    private WebElement ruleDetailsText;

    @FindBy(css = ".container-fluid h1")
    private WebElement ruleTitleText;

    private final WebDriver driver;

    public ParasoftWarningsPage(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    public void clickModulesLink() {
        ElementUtils.scrollTo(modulesLink, driver);
        ElementUtils.waitUntilVisible(driver, modulesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, modulesLink);
    }

    public String getModulesInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, moduleInfoText, moduleInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return moduleInfoText.getText();
    }

    public void clickNamespacesLink() {
        ElementUtils.scrollTo(namespacesLink, driver);
        ElementUtils.waitUntilVisible(driver, namespacesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, namespacesLink);
    }

    public String getNamespacesInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, packageInfoText, packageInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return packageInfoText.getText();
    }

    public void clickFoldersLink() {
        ElementUtils.scrollTo(foldersLink, driver);
        ElementUtils.waitUntilVisible(driver, foldersLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, foldersLink);
    }

    public String getFolderInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, folderInfoText, folderInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return folderInfoText.getText();
    }

    public void clickPackagesLink() {
        ElementUtils.scrollTo(packagesLink, driver);
        ElementUtils.waitUntilVisible(driver, packagesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, packagesLink);
    }

    public String getPackageInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, packageInfoText, packageInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return packageInfoText.getText();
    }

    public void clickFilesLink() {
        ElementUtils.waitUntilVisible(driver, filesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, filesLink);
    }

    public String getFileInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, fileInfoText, fileInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return fileInfoText.getText();
    }

    public void clickCategoriesLink() {
        ElementUtils.waitUntilVisible(driver, categoriesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, categoriesLink);
    }

    public String getCategoryInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, categoryInfoText, categoryInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return categoryInfoText.getText();
    }

    public void clickTypesLink() {
        ElementUtils.waitUntilVisible(driver, typesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, typesLink);
    }

    public String getTypeInfoText() {
        ElementUtils.waitUntilElementTextAppear(driver, typeInfoText, typeInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return typeInfoText.getText();
    }

    public void clickIssuesLink() {
        ElementUtils.waitUntilVisible(driver, issuesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, issuesLink);
    }

    public String getIssuesInfoText() {
        ElementUtils.waitUntilVisible(driver, issuesPaginate, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilElementTextAppear(driver, issuesInfoText, issuesInfoText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return issuesInfoText.getText();
    }

    public void clickRuleTypeLink(String ruleType) {
        WebElement ruleTypeLink = driver.findElement(By.xpath("//*[@id='type']//a[text()='" + ruleType + "']"));
        ElementUtils.waitUntilVisible(driver, ruleTypeLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, ruleTypeLink);
    }

    public String getRuleTitleText() {
        ElementUtils.waitUntilVisible(driver, ruleTitleText, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilElementTextAppear(driver, ruleTitleText, ruleTitleText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return ruleTitleText.getText();
    }

    public void clickOpenIconButton() {
        ElementUtils.scrollTo(openIcon, driver);
        ElementUtils.waitUntilVisible(driver, openIcon, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, openIcon);
    }

    public String getRuleDetailsText() {
        ElementUtils.waitUntilVisible(driver, ruleDetailsText, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilElementTextAppear(driver, ruleDetailsText, ruleDetailsText.getText(), Properties.WAIT_FOR_TIMEOUT);
        return ruleDetailsText.getText();
    }
}