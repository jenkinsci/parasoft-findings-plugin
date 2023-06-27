package com.parasoft.findings.jenkins.common;

public class ParasoftWarningsInformation {
    String projectName;
    String packagesTotalNumber;
    String filesTotalNumber;
    String typesTotalNumber;
    String issuesInfo;

    public ParasoftWarningsInformation(String projectName, String packagesTotalNumber, String filesTotalNumber, String typesTotalNumber, String issuesInfo) {
        this.projectName = projectName;
        this.packagesTotalNumber = packagesTotalNumber;
        this.filesTotalNumber = filesTotalNumber;
        this.typesTotalNumber = typesTotalNumber;
        this.issuesInfo = issuesInfo;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getPackagesTotalNumber() {
        return packagesTotalNumber;
    }

    public String getFilesTotalNumber() {
        return filesTotalNumber;
    }

    public String getTypesTotalNumber() {
        return typesTotalNumber;
    }

    public String getIssuesInfo() {
        return issuesInfo;
    }
}
