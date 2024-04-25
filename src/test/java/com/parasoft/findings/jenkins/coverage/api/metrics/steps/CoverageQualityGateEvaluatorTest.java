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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.hm.hafner.util.FilteredLog;
import io.jenkins.plugins.util.NullResultHandler;
import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.util.assertions.Assertions.*;

class CoverageQualityGateEvaluatorTest extends AbstractCoverageTest {
    @Test
    void shouldBeInactiveIfGatesAreEmpty() {
        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(new ArrayList<>(), createStatistics());

        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));

        assertThat(result).hasNoMessages().isInactive().isSuccessful().hasOverallStatus(QualityGateStatus.INACTIVE);
    }

    @Test
    void shouldPassForTooLowThresholds() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();

        qualityGates.add(new CoverageQualityGate(0, Baseline.PROJECT, QualityGateCriticality.UNSTABLE));qualityGates.add(new CoverageQualityGate(0, Baseline.MODIFIED_LINES, QualityGateCriticality.UNSTABLE));
        qualityGates.add(new CoverageQualityGate(0, Baseline.MODIFIED_LINES, QualityGateCriticality.UNSTABLE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());

        assertThat(evaluator).isEnabled();

        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));

        assertThat(result).hasOverallStatus(QualityGateStatus.PASSED).isSuccessful().isNotInactive().hasMessages(
                "[Overall project - Coverage]: ≪Success≫ - (Actual value: 50.00%, Quality gate: 0.00)",
                "[Modified code lines - Coverage]: ≪Success≫ - (Actual value: 50.00%, Quality gate: 0.00)");
    }

    @Test
    void shouldSkipIfValueNotDefined() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();

        qualityGates.add(new CoverageQualityGate(0, Baseline.MODIFIED_LINES, QualityGateCriticality.UNSTABLE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createOnlyProjectStatistics());

        assertThat(evaluator).isEnabled();

        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));

        assertThat(result).hasOverallStatus(QualityGateStatus.INACTIVE).isInactive().hasMessages(
                "[Modified code lines - Coverage]: ≪Not built≫ - (Actual value: N/A, Quality gate: 0.00)");
    }

    @Test
    void shouldReportUnstableIfBelowThreshold() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();

        qualityGates.add(new CoverageQualityGate(51.0, Baseline.PROJECT, QualityGateCriticality.UNSTABLE));
        qualityGates.add(new CoverageQualityGate(51.0, Baseline.MODIFIED_LINES, QualityGateCriticality.UNSTABLE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());
        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));

        assertThat(result).hasOverallStatus(QualityGateStatus.WARNING).isNotSuccessful().isNotInactive().hasMessages(
                "[Overall project - Coverage]: ≪Unstable≫ - (Actual value: 50.00%, Quality gate: 51.00)",
                "[Modified code lines - Coverage]: ≪Unstable≫ - (Actual value: 50.00%, Quality gate: 51.00)");
    }

    @Test
    void shouldReportUnstableIfLargerThanThreshold() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();

        qualityGates.add(new CoverageQualityGate(-149.0, Baseline.PROJECT, QualityGateCriticality.UNSTABLE));
        qualityGates.add(new CoverageQualityGate(14, Baseline.PROJECT, QualityGateCriticality.UNSTABLE));
        qualityGates.add(new CoverageQualityGate(999, Baseline.MODIFIED_LINES, QualityGateCriticality.UNSTABLE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());
        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));

        assertThat(result).hasOverallStatus(QualityGateStatus.WARNING).isNotSuccessful().isNotInactive().hasMessages(
                "[Overall project - Coverage]: ≪Success≫ - (Actual value: 50.00%, Quality gate: 0.00)",
                "[Overall project - Coverage]: ≪Success≫ - (Actual value: 50.00%, Quality gate: 14.00)",
                "[Modified code lines - Coverage]: ≪Unstable≫ - (Actual value: 50.00%, Quality gate: 100.00)");
    }

    @Test
    void shouldReportUnstableIfWorseAndSuccessIfBetter2() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();

        var minimum = 0;
        qualityGates.add(new CoverageQualityGate(minimum, Baseline.PROJECT, QualityGateCriticality.UNSTABLE));
        qualityGates.add(new CoverageQualityGate(minimum, Baseline.MODIFIED_LINES, QualityGateCriticality.UNSTABLE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());
        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));

        assertThat(result).hasOverallStatus(QualityGateStatus.PASSED);
    }

    @Test
    void shouldReportFailureIfBelowThreshold() {
        QualityGateResult result = createQualityGateResult();

        assertThat(result).hasOverallStatus(QualityGateStatus.FAILED).isNotSuccessful().isNotInactive().hasMessages(
                "[Overall project - Coverage]: ≪Failed≫ - (Actual value: 50.00%, Quality gate: 51.00)",
                "[Modified code lines - Coverage]: ≪Failed≫ - (Actual value: 50.00%, Quality gate: 51.00)");
    }

    static QualityGateResult createQualityGateResult() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(new CoverageQualityGate(51.0, Baseline.PROJECT, QualityGateCriticality.FAILURE));
        qualityGates.add(new CoverageQualityGate(51.0, Baseline.MODIFIED_LINES, QualityGateCriticality.FAILURE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());

        return evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));
    }

    @Test
    void shouldOverwriteStatus() {
        Collection<CoverageQualityGate> qualityGates = new ArrayList<>();

        qualityGates.add(new CoverageQualityGate(51.0, Baseline.PROJECT, QualityGateCriticality.FAILURE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());
        assertThatStatusWillBeOverwritten(evaluator);
    }

    @Test
    void shouldAddAllQualityGates() {
        Collection<CoverageQualityGate> qualityGates = List.of(
                new CoverageQualityGate(51.0, Baseline.PROJECT, QualityGateCriticality.FAILURE));

        CoverageQualityGateEvaluator evaluator = new CoverageQualityGateEvaluator(qualityGates, createStatistics());

        assertThatStatusWillBeOverwritten(evaluator);
    }

    private static void assertThatStatusWillBeOverwritten(final CoverageQualityGateEvaluator evaluator) {
        QualityGateResult result = evaluator.evaluate(new NullResultHandler(), new FilteredLog("Errors"));
        assertThat(result).hasOverallStatus(QualityGateStatus.FAILED).isNotSuccessful().hasMessages(
                "[Overall project - Coverage]: ≪Failed≫ - (Actual value: 50.00%, Quality gate: 51.00)");
    }
}
