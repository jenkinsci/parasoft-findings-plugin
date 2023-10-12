/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
