package com.parasoft.findings.jenkins.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class WebDriverInitialization {
    public static WebDriver init() throws Exception {
        String baseUrl = Properties.BASE_URL;
        String dotNetInstallPath = Properties.DOTNET_HOME;
        String dotTestInstallPath = Properties.DOTTEST_INSTALL_DIR;
        String cppTestInstallPath = Properties.CPPTEST_STD_INSTALL_DIR;

        if(baseUrl == null || baseUrl.isBlank()) {
            throw new Exception(Properties.BASE_URL_ERROR_MESSAGE);
        }

        if(dotNetInstallPath == null || dotNetInstallPath.isBlank()) {
            throw new Exception(Properties.DOTNET_HOME_ERROR_MESSAGE);
        }

        if(dotTestInstallPath == null || dotTestInstallPath.isBlank()) {
            throw new Exception(Properties.DOTTEST_INSTALL_ERROR_DIR_MESSAGE);
        }

        if(cppTestInstallPath == null || cppTestInstallPath.isBlank()) {
            throw new Exception(Properties.CPPTEST_STD_INSTALL_ERROR_DIR_MESSAGE);
        }

        ChromeOptions chromeOptions = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("intl.accept_languages", "en-US,en");
        chromeOptions.setExperimentalOption("prefs", prefs);
        return new ChromeDriver(chromeOptions);
    }
}
