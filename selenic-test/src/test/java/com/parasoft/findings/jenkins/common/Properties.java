package com.parasoft.findings.jenkins.common;

public class Properties {
    public static final String BASE_URL = "http://localhost:8080";
    public static final String DASHBOARD_PAGE = BASE_URL + "/jenkins";

    public static final String DOTTEST_JOB_NAME = "cicd.findings.dottest.FunctionalTest";
    public static final String JTEST_JOB_NAME = "cicd.findings.jtest.FunctionalTest";
    public static final String CPPTEST_JOB_NAME = "cicd.findings.cpptest.FunctionalTest";
    public static final String DOTTEST_PROJECT_GIT_URL = "https://code.parasoftcn.com/scm/pf/bankexample.net.git";
    public static final String JTEST_PROJECT_GIT_URL = "https://code.parasoftcn.com/scm/pf/javaprojecttemplate.git";
    public static final String CPPTEST_PROJECT_GIT_URL = "https://code.parasoftcn.com/scm/pf/cppprojecttemplate.git";
    public static final String DOTTEST_PROJECT_COMMAND= "\"D:\\Software\\ParasoftTools\\dottest\\dottestcli.exe\" " +
                                                        "-solution \"BankExample.NET.sln\" " +
                                                        "-config \"dottest_settings.properties\" " +
                                                        "-settings \"localsettings.properties\" " +
                                                        "-report \"build/reports/dottest/static\"";
    public static final String JTEST_PROJECT_COMMAND= "mvn jtest:jtest -Djtest.config=\"builtin://Recommended Rules\" " +
                                                        "-Djtest.settings=\"./localsettings.properties\" " +
                                                        "-Djtest.report=\"./build/report/jtest/static\"";
    public static final String CPPTEST_PROJECT_COMMAND= "set \"PROJECT_NAME=cpp-project-template\"\n" +
                                                        "set \"BUILD_DIR=build\"\n" +
                                                        "set \"CPPTEST_STD_HOME=D:/Software/ParasoftTools/cpptest\"\n" +
                                                        "rem \"Compile project\"\n" +
                                                        "cd \"%PROJECT_NAME%\"\n" +
                                                        "rmdir /s /q \"%BUILD_DIR%\"\n" +
                                                        "mkdir \"%BUILD_DIR%\"\n" +
                                                        "cd \"%BUILD_DIR%\"\n" +
                                                        "cmake -DCMAKE_EXPORT_COMPILE_COMMANDS=ON -S \"../\" " +
                                                        "-DCMAKE_BUILD_TYPE=Release -G \"MinGW Makefiles\"\n" +
                                                        "\"%CPPTEST_STD_HOME%/cpptestcli.exe\" -config \"../cpptest_settings.properties\" " +
                                                        "-settings \"../localsettings.properties\" -report \"./reports/cpptest-std/static/\" " +
                                                        "-compiler gcc_9-64 -input compile_commands.json";

    public static final String PARASOFT_FINDINGS_PLUGIN_DROPDOWN_OPTION = "Parasoft Findings";
    public static final String PARASOFT_FINDINGS_PLUGIN_SETTINGS_TEXT = "localsettings.properties";

    public static final String DOTTEST_PACKAGES_TOTAL_NUMBER_ASSERTATION = "42";
    public static final String DOTTEST_FILES_TOTAL_NUMBER_ASSERTATION = "42";
    public static final String DOTTEST_TYPES_TOTAL_NUMBER_ASSERTATION ="42";
    public static final String DOTTEST_ISSUES_INFO_ASSERTATION ="Showing 1 to 10 of 42 entries";

    public static final String JTEST_PACKAGES_TOTAL_NUMBER_ASSERTATION = "";
    public static final String JTEST_FILES_TOTAL_NUMBER_ASSERTATION = "";
    public static final String JTEST_TYPES_TOTAL_NUMBER_ASSERTATION ="";
    public static final String JTEST_ISSUES_INFO_ASSERTATION ="Showing 1 to 1 of 1 entries";

    public static final String CPPTEST_PACKAGES_TOTAL_NUMBER_ASSERTATION = "1289";
    public static final String CPPTEST_FILES_TOTAL_NUMBER_ASSERTATION = "1289";
    public static final String CPPTEST_TYPES_TOTAL_NUMBER_ASSERTATION ="1289";
    public static final String CPPTEST_ISSUES_INFO_ASSERTATION ="Showing 1 to 10 of 1,289 entries";

    public static final int WAIT_FOR_TIMEOUT = 60;
    public static final int WAIT_FOR_BUILF_FINISHED_TIMEOUT = 240 * 1000;
}
