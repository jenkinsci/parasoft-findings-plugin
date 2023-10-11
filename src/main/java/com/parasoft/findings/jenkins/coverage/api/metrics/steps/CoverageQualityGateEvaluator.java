package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.util.Collection;
import java.util.Locale;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.ElementFormatter;
import io.jenkins.plugins.util.QualityGateEvaluator;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

/**
 * Evaluates a given set of quality gates.
 *
 * @author Johannes Walter
 */
class CoverageQualityGateEvaluator extends QualityGateEvaluator<CoverageQualityGate> {
    private static final ElementFormatter FORMATTER = new ElementFormatter();
    private final CoverageStatistics statistics;

    CoverageQualityGateEvaluator(final Collection<? extends CoverageQualityGate> qualityGates, final CoverageStatistics statistics) {
        super(qualityGates);

        this.statistics = statistics;
    }

    @Override
    protected void evaluate(final CoverageQualityGate qualityGate, final QualityGateResult result) {
        var baseline = qualityGate.getType();
        var possibleValue = statistics.getValue(baseline, qualityGate.getMetric());
        if (possibleValue.isPresent()) {
            var actualValue = possibleValue.get();

            var status = actualValue.isOutOfValidRange(
                    qualityGate.getThreshold()) ? qualityGate.getStatus() : QualityGateStatus.PASSED;
            result.add(qualityGate, status, FORMATTER.format(actualValue, Locale.ENGLISH));
        }
        else {
            result.add(qualityGate, QualityGateStatus.INACTIVE, Messages.Coverage_Not_Available());
        }
    }
}
