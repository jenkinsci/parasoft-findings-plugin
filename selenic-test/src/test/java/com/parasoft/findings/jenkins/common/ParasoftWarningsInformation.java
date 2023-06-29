package com.parasoft.findings.jenkins.common;

public class ParasoftWarningsInformation {
    String projectName;
    String modulesEntries;
    String packagesEntries;
    String filesEntries;
    String typesEntries;
    String issuesEntries;

    public ParasoftWarningsInformation(String projectName, String modulesEntries, String packagesEntries,
                                       String filesEntries, String typesEntries, String issuesEntries) {
        this.projectName = projectName;
        this.modulesEntries = modulesEntries;
        this.packagesEntries = packagesEntries;
        this.filesEntries = filesEntries;
        this.typesEntries = typesEntries;
        this.issuesEntries = issuesEntries;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getModulesEntries() {
        return modulesEntries;
    }

    public String getPackagesEntries() {
        return packagesEntries;
    }

    public String getFilesEntries() {
        return filesEntries;
    }

    public String getTypesEntries() {
        return typesEntries;
    }

    public String getIssuesEntries() {
        return issuesEntries;
    }
}
