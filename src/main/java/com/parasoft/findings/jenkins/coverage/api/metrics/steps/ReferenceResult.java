package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

public class ReferenceResult {

    static final String DEFAULT_REFERENCE_BUILD_IDENTIFIER = "default";

    private final ReferenceStatus status;

    private final String referenceBuild;

    public ReferenceResult(ReferenceStatus status, String referenceBuild) {
        this.status = status;
        this.referenceBuild = referenceBuild;
    }

    public ReferenceStatus getStatus() {
        return status;
    }

    public String getReferenceBuild() {
        return referenceBuild;
    }

    public enum ReferenceStatus {
        OK,
        NO_REF_BUILD,
        REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE,
        NO_CVG_DATA_IN_REF_BUILD;
    }

}
