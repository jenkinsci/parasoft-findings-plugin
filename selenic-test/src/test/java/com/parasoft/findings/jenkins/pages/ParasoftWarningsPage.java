package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class ParasoftWarningsPage {
    @FindBy(id = "moduleName_info")
    private WebElement moduleInfo;

    @FindBy(id = "folder_info")
    private WebElement folderInfo;

    @FindBy(id = "packageName_info")
    private WebElement packageInfo;

    @FindBy(id = "fileName_info")
    private WebElement fileInfo;

    @FindBy(id = "category_info")
    private WebElement categoryInfo;

    @FindBy(id = "type_info")
    private WebElement typeInfo;

    @FindBy(id = "issues_info")
    private WebElement issuesInfo;

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

    public String getModulesInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, moduleInfo, moduleInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return moduleInfo.getText();
    }

    public void clickNamespacesLink() {
        ElementUtils.scrollTo(namespacesLink, driver);
        ElementUtils.waitUntilVisible(driver, namespacesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, namespacesLink);
    }

    public String getNamespacesInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, packageInfo, packageInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return packageInfo.getText();
    }

    public void clickFoldersLink() {
        ElementUtils.scrollTo(foldersLink, driver);
        ElementUtils.waitUntilVisible(driver, foldersLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, foldersLink);
    }

    public String getFolderInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, folderInfo, folderInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return folderInfo.getText();
    }

    public void clickPackagesLink() {
        ElementUtils.scrollTo(packagesLink, driver);
        ElementUtils.waitUntilVisible(driver, packagesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, packagesLink);
    }

    public String getPackageInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, packageInfo, packageInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return packageInfo.getText();
    }

    public void clickFilesLink() {
        ElementUtils.waitUntilVisible(driver, filesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, filesLink);
    }

    public String getFileInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, fileInfo, fileInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return fileInfo.getText();
    }

    public void clickCategoriesLink() {
        ElementUtils.waitUntilVisible(driver, categoriesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, categoriesLink);
    }

    public String getCategoryInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, categoryInfo, categoryInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return categoryInfo.getText();
    }

    public void clickTypesLink() {
        ElementUtils.waitUntilVisible(driver, typesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, typesLink);
    }

    public String getTypeInfo() {
        ElementUtils.waitUntilElementTextAppear(driver, typeInfo, typeInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return typeInfo.getText();
    }

    public void clickIssuesLink() {
        ElementUtils.waitUntilVisible(driver, issuesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, issuesLink);
    }

    public String getIssuesInfo() {
        ElementUtils.waitUntilVisible(driver, issuesPaginate, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilElementTextAppear(driver, issuesInfo, issuesInfo.getText(), Properties.WAIT_FOR_TIMEOUT);
        return issuesInfo.getText();
    }
}