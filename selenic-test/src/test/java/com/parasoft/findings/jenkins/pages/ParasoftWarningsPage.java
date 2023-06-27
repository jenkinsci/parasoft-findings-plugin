package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class ParasoftWarningsPage {
    @FindBy(xpath = "//*[@id='packageName']/tfoot/tr/td[2]")
    private WebElement namespacesTotalNumber;

    @FindBy(xpath = "//*[@id='folder']/tfoot/tr/td[2]")
    private WebElement foldersTotalNumber;

    @FindBy(xpath = "//*[@id='package']/tfoot/tr/td[2]")
    private WebElement packagesTotalNumber;

    @FindBy(xpath = "//*[@id='fileName']/tfoot/tr/td[2]")
    private WebElement filesTotalNumber;

    @FindBy(xpath = "//*[@id='type']/tfoot/tr/td[2]")
    private WebElement typesTotalNumber;

    @FindBy(xpath = "//*[@id='issues_info']")
    private WebElement issuesInfo;

    @FindBy(linkText = "Namespaces")
    private WebElement namespacesLink;

    @FindBy(linkText = "Folders")
    private WebElement foldersLink;

    @FindBy(linkText = "Packages")
    private WebElement packagesLink;

    @FindBy(linkText = "Files")
    private WebElement filesLink;

    @FindBy(linkText = "Types")
    private WebElement typesLink;

    @FindBy(linkText = "Issues")
    private WebElement issuesLink;

    @FindBy(xpath = "//*[@id='issues_paginate']")
    private WebElement issuesPaginate;

    private final WebDriver driver;

    public ParasoftWarningsPage(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    public void clickNamespacesLink() {
        ElementUtils.scrollTo(namespacesLink, driver);
        ElementUtils.waitUntilVisible(driver, namespacesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, namespacesLink);
    }

    public String getNamespacesTotalNumber() {
        ElementUtils.waitUntilElementTextAppear(driver, namespacesTotalNumber, namespacesTotalNumber.getText(), Properties.WAIT_FOR_TIMEOUT);
        return namespacesTotalNumber.getText();
    }

    public void clickFoldersLink() {
        ElementUtils.scrollTo(foldersLink, driver);
        ElementUtils.waitUntilVisible(driver, foldersLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, foldersLink);
    }

    public String getFoldersTotalNumber() {
        ElementUtils.waitUntilElementTextAppear(driver, foldersTotalNumber, foldersTotalNumber.getText(), Properties.WAIT_FOR_TIMEOUT);
        return foldersTotalNumber.getText();
    }

    public void clickPackagesLink() {
        ElementUtils.scrollTo(packagesLink, driver);
        ElementUtils.waitUntilVisible(driver, packagesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, packagesLink);
    }

    public String getPackagesTotalNumber() {
        ElementUtils.waitUntilElementTextAppear(driver, packagesTotalNumber, packagesTotalNumber.getText(), Properties.WAIT_FOR_TIMEOUT);
        return packagesTotalNumber.getText();
    }

    public void clickFilesLink() {
        ElementUtils.waitUntilVisible(driver, filesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, filesLink);
    }

    public String getFilesTotalNumber() {
        ElementUtils.waitUntilElementTextAppear(driver, filesTotalNumber, filesTotalNumber.getText(), Properties.WAIT_FOR_TIMEOUT);
        return filesTotalNumber.getText();
    }

    public void clickTypesLink() {
        ElementUtils.waitUntilVisible(driver, typesLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, typesLink);
    }

    public String getTypesTotalNumber() {
        ElementUtils.waitUntilElementTextAppear(driver, typesTotalNumber, typesTotalNumber.getText(), Properties.WAIT_FOR_TIMEOUT);
        return typesTotalNumber.getText();
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