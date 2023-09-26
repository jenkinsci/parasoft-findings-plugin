package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.util.Optional;

public class ReferenceBuildActionResult {

    private final Optional<CoverageBuildAction> possibleReferenceResult;

    private final ReferenceResult referenceResult;

    public ReferenceBuildActionResult(Optional<CoverageBuildAction> possibleReferenceResult, ReferenceResult referenceResult) {
        this.possibleReferenceResult = possibleReferenceResult;
        this.referenceResult = referenceResult;
    }

    public Optional<CoverageBuildAction> getPossibleReferenceResult() {
        return possibleReferenceResult;
    }

    public ReferenceResult getReferenceResult() {
        return referenceResult;
    }

}
