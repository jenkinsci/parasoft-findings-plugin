package com.parasoft.findings.jenkins.common;

public class Properties {
    public static final String BASE_URL = System.getProperty("BASE_URL", "http://localhost:8080/jenkins");
    public static final String DOTNET_HOME = System.getProperty("DOTNET_HOME", "C:/Program Files/dotnet");
    public static final String DOTTEST_INSTALL_DIR = System.getProperty("DOTTEST_INSTALL_DIR", "C:/ParasoftTools/dottest");
    public static final String CPPTEST_STD_INSTALL_DIR = System.getProperty("CPPTEST_STD_INSTALL_DIR", "C:/ParasoftTools/cpptest_std");

    public static final String DOTTEST_JOB_NAME = "cicd.findings.dottest.FunctionalTest";
    public static final String JTEST_JOB_NAME = "cicd.findings.jtest.FunctionalTest";
    public static final String CPPTEST_JOB_NAME = "cicd.findings.cpptest.FunctionalTest";
    public static final String DOTTEST_PROJECT_GIT_URL = "https://code.parasoftcn.com/scm/pf/bankexample.net.git";
    public static final String JTEST_PROJECT_GIT_URL = "https://code.parasoftcn.com/scm/pf/javaprojecttemplate.git";
    public static final String CPPTEST_PROJECT_GIT_URL = "https://code.parasoftcn.com/scm/pf/cppprojecttemplate.git";
    public static final String DOTTEST_PROJECT_COMMAND= "set \"DOTNET_HOME=" + DOTNET_HOME + "\"\n" +
                                                        "set \"Path=%DOTNET_HOME%;\"\n" +
                                                        "\"" + DOTTEST_INSTALL_DIR + "/dottestcli.exe\" " +
                                                        "-solution \"BankExample.NET.sln\" " +
                                                        "-config \"./dottest_settings.properties\" " +
                                                        "-settings \"./localsettings.properties\" " +
                                                        "-report \"build/reports/dottest/static\"";
    public static final String JTEST_PROJECT_COMMAND= "mvn jtest:jtest -Djtest.config=\"./jtest_settings.properties\" " +
                                                        "-Djtest.settings=\"./localsettings.properties\" " +
                                                        "-Djtest.report=\"./build/report/jtest/static\"";
    public static final String CPPTEST_PROJECT_COMMAND= "set \"PROJECT_NAME=cpp-project-template\"\n" +
                                                        "set \"BUILD_DIR=build\"\n" +
                                                        "cd \"%PROJECT_NAME%\"\n" +
                                                        "rmdir /s /q \"%BUILD_DIR%\"\n" +
                                                        "mkdir \"%BUILD_DIR%\"\n" +
                                                        "cd \"%BUILD_DIR%\"\n" +
                                                        "cmake -DCMAKE_EXPORT_COMPILE_COMMANDS=ON -S \"../\" " +
                                                        "-DCMAKE_BUILD_TYPE=Release -G \"MinGW Makefiles\"\n" +
                                                        "\"" + CPPTEST_STD_INSTALL_DIR + "/cpptestcli.exe\" " +
                                                        "-config \"../cpptest_settings.properties\" " +
                                                        "-settings \"../localsettings.properties\" -report \"./reports/cpptest-std/static/\" " +
                                                        "-compiler gcc_9-64 -input compile_commands.json";

    public static final String PARASOFT_FINDINGS_PLUGIN_DROPDOWN_OPTION = "Parasoft Findings";
    public static final String PARASOFT_FINDINGS_PLUGIN_SETTINGS_TEXT = "localsettings.properties";

    public static final String DOTTEST_NAMESPACES_ENTRIES_ASSERTATION = "3 entries";
    public static final String DOTTEST_FILES_ENTRIES_ASSERTATION = "18 entries";
    public static final String DOTTEST_CATEGORIES_ENTRIES_ASSERTATION = "33 entries";
    public static final String DOTTEST_TYPES_ENTRIES_ASSERTATION = "99 entries";
    public static final String DOTTEST_ISSUES_ENTRIES_ASSERTATION = "1,552 entries";
    public static final String DOTTEST_RULE_TYPE_ASSERTATION = "Type BRM.CMT.MSC";

    public static final String JTEST_PACKAGES_ENTRIES_ASSERTATION = "6 entries";
    public static final String JTEST_FILES_ENTRIES_ASSERTATION = "6 entries";
    public static final String JTEST_CATEGORIES_ENTRIES_ASSERTATION ="32 entries";
    public static final String JTEST_TYPES_ENTRIES_ASSERTATION = "68 entries";
    public static final String JTEST_ISSUES_ENTRIES_ASSERTATION = "319 entries";
    public static final String JTEST_RULE_TYPE_ASSERTATION = "Type APSC_DV.001460.SIO";

    public static final String CPPTEST_MODULES_ENTRIES_ASSERTATION = "5 entries";
    public static final String CPPTEST_FOLDERS_ENTRIES_ASSERTATION = "6 entries";
    public static final String CPPTEST_FILES_ENTRIES_ASSERTATION = "8 entries";
    public static final String CPPTEST_CATEGORIES_ENTRIES_ASSERTATION = "124 entries";
    public static final String CPPTEST_TYPES_ENTRIES_ASSERTATION = "331 entries";
    public static final String CPPTEST_ISSUES_ENTRIES_ASSERTATION = "1,289 entries";
    public static final String CPPTEST_RULE_TYPE_ASSERTATION = "Type APSC_DV-003110-a";

    public static final int WAIT_FOR_TIMEOUT = 60; // seconds
    public static final int WAIT_FOR_BUILD_FINISHED_TIMEOUT = 7 * 60 * 1000; // milliseconds
}
