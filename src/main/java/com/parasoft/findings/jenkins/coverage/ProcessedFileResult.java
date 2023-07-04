package com.parasoft.findings.jenkins.coverage;

import java.io.Serializable;

public class ProcessedFileResult implements Serializable {
    private static final long serialVersionUID = 2393265115219226404L;

    private final String coberturaPattern;

    private final String generatedCoverageBuildDir;

    public ProcessedFileResult(String coberturaPattern, String generatedCoverageBuildDir) {
        this.coberturaPattern = coberturaPattern;
        this.generatedCoverageBuildDir = generatedCoverageBuildDir;
    }

    public String getCoberturaPattern() {
        return coberturaPattern;
    }

    public String getGeneratedCoverageBuildDir() {
        return generatedCoverageBuildDir;
    }
}
