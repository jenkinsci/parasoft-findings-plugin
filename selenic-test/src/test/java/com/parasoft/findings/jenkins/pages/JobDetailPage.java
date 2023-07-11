package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobDetailPage {

    public static final String BUILD_STATUS_TOOLTIP_ATTRIBUTE = "tooltip";
    public static final String BUILD_STATUS_TOOLTIP_ATTRIBUTE_VALUE_SUCCESS = "Success";
    public static final String BUILD_STATUS_TOOLTIP_ATTRIBUTE_VALUE_IN_PROGRESS = "In progress";

    @FindBy(linkText = "Build Now")
    private WebElement buildNowLink;

    @FindBy(linkText = "Parasoft Warnings")
    private WebElement parasoftWarningsLink;

    @FindBy(linkText = "Delete Project")
    private WebElement deleteProjectLink;

    @FindBy(linkText = "Status")
    private WebElement projectStatusLink;

    @FindBy(xpath = "//div[@class='build-icon'][1]/a")
    private WebElement buildIconLink;

    private final By buildIconLinkLocator = By.xpath("//div[@class='build-icon'][1]/a");

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
        ElementUtils.waitUntilVisible(driver, buildIconLink, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilElementAttributeNotContains(driver, buildIconLinkLocator, BUILD_STATUS_TOOLTIP_ATTRIBUTE,
                BUILD_STATUS_TOOLTIP_ATTRIBUTE_VALUE_IN_PROGRESS, Properties.WAIT_FOR_BUILD_FINISHED_TIMEOUT, true);
        assertTrue(driver.findElement(buildIconLinkLocator).getAttribute(BUILD_STATUS_TOOLTIP_ATTRIBUTE).contains(BUILD_STATUS_TOOLTIP_ATTRIBUTE_VALUE_SUCCESS));
        ElementUtils.clickElementUseJs(driver, projectStatusLink);
    }
}