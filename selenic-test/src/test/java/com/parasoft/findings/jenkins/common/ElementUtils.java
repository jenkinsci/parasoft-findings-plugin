package com.parasoft.findings.jenkins.common;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class ElementUtils {
    public static WebElement waitUntilClickable(WebDriver driver, WebElement element, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.ignoring(StaleElementReferenceException.class);
        return wait.until(elementToBeClickable(element));
    }

    public static WebElement waitUntilVisible(WebDriver driver, WebElement element, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.ignoring(StaleElementReferenceException.class);
        return wait.until(visibilityOf(element));
    }

    public static void clickElementUseJs(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        js.executeScript("arguments[0].click();",element);
    }

    public static void waitUntilElementTextAppear(WebDriver driver, WebElement element, String text, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver,timeOutInSeconds);
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
        wait.until(visibilityOf(element));
    }

    public static void waitUntilElementAttributeNotContains(WebDriver driver, By locator, String key, String value, long timeOutInSeconds, boolean ignoreStaleElementReferenceException) {
        WebDriverWait wait = new WebDriverWait(driver,timeOutInSeconds);
        doWaitUntilElementAttributeNotContains(driver, wait, locator, key, value, ignoreStaleElementReferenceException);
    }

    private static void doWaitUntilElementAttributeNotContains(WebDriver driver, WebDriverWait wait, By locator, String key, String value, boolean ignoreStaleElementReferenceException) {
        try {
            wait.until(visibilityOf(driver.findElement(locator)));
            wait.until(not(ExpectedConditions.attributeContains(locator, key, value)));
        } catch (StaleElementReferenceException e) {
            if (ignoreStaleElementReferenceException) {
                doWaitUntilElementAttributeNotContains(driver, wait, locator, key, value, true);
            } else {
                throw e;
            }
        }
    }

    public static void scrollTo(WebElement element, WebDriver driver) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView(false);", element);
    }
}
