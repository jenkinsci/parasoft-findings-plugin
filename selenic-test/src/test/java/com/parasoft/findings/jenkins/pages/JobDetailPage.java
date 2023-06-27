package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class JobDetailPage {
    @FindBy(linkText = "Build Now")
    private WebElement buildNowLink;

    @FindBy(linkText = "Parasoft Warnings")
    private WebElement parasoftWarningsLink;

    @FindBy(linkText = "Delete Project")
    private WebElement deleteProjectLink;

    @FindBy(xpath = "//*[@id='breadcrumbs']/li[3]/a")
    private WebElement projectNameLink;

    private final WebDriver driver;

    public JobDetailPage(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    public void clickBuildNowLink() {
        ElementUtils.waitUntilVisible(driver, buildNowLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, buildNowLink);
    }

    public void clickParasoftWarningsLink () {
        ElementUtils.waitUntilVisible(driver, parasoftWarningsLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, parasoftWarningsLink);
    }

    public void clickDeleteProjectLink() {
        ElementUtils.waitUntilVisible(driver, deleteProjectLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, deleteProjectLink);
    }

    public void waitBuildFinished(WebDriver driver) {
        try {
            Thread.sleep(Properties.WAIT_FOR_BUILF_FINISHED_TIMEOUT);
            ElementUtils.clickElementUseJs(driver, projectNameLink);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}