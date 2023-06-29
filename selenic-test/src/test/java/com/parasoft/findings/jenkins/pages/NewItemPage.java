package com.parasoft.findings.jenkins.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.parasoft.findings.jenkins.common.ElementUtils;
import com.parasoft.findings.jenkins.common.Properties;

public class NewItemPage {
    @FindBy(name = "name")
    private WebElement nameInput;

    @FindBy(className = "hudson_model_FreeStyleProject")
    private WebElement freestyleProjectText;

    @FindBy(xpath = "//*[@id='ok-button']")
    private WebElement oKButton;

    private final WebDriver driver;

    public NewItemPage(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Properties.WAIT_FOR_TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    public void setItemNameInput(String text) {
        ElementUtils.waitUntilVisible(driver, nameInput, Properties.WAIT_FOR_TIMEOUT).clear();
        nameInput.sendKeys(text);
    }

    public void clickFreestyleProjectText() {
        ElementUtils.waitUntilVisible(driver,freestyleProjectText,Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilClickable(driver,freestyleProjectText,Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, freestyleProjectText);
    }

    public void clickOKButton() {
        ElementUtils.waitUntilClickable(driver,oKButton,Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.waitUntilClickable(driver, oKButton, Properties.WAIT_FOR_TIMEOUT);
        ElementUtils.clickElementUseJs(driver, oKButton);
    }
}