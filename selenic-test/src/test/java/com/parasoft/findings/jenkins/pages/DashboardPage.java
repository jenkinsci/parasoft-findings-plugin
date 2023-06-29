package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class DashboardPage {
    @FindBy(linkText = "New Item")
    private WebElement newItemLink;

    private final WebDriver driver;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    public void clickNewItemLink() {
        ElementUtils.waitUntilVisible(driver, newItemLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, newItemLink);
    }
}