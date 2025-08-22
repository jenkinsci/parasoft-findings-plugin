/*
 * MIT License
 *
 * Copyright (c) 2022 Dr. Ullrich Hafner
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

package com.parasoft.findings.jenkins.coverage.model;

import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.DefaultLocale;

import nl.jqno.equalsverifier.EqualsVerifier;

import static com.parasoft.findings.jenkins.coverage.model.Coverage.*;
import static com.parasoft.findings.jenkins.coverage.Assertions.*;

/**
 * Tests the class {@link Coverage}.
 *
 * @author Ullrich Hafner
 */
@DefaultLocale("en")
class CoverageTest {

    private static final Coverage NO_COVERAGE = new CoverageBuilder()
            .setMetric(Metric.LINE)
            .setCovered(0)
            .setMissed(0)
            .build();

    @Test
    void shouldComputeDelta() {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage worse = builder.setCovered(0).setMissed(2).build();
        Coverage ok = builder.setCovered(1).setMissed(1).build();
        Coverage better = builder.setCovered(2).setMissed(0).build();

        assertThat(worse.delta(better).doubleValue()).isEqualTo(getDelta("-1/1"));
        assertThat(better.delta(worse).doubleValue()).isEqualTo(getDelta("1/1"));
        assertThat(worse.delta(ok).doubleValue()).isEqualTo(getDelta("-1/2"));
        assertThat(ok.delta(worse).doubleValue()).isEqualTo(getDelta("1/2"));
    }

    @Test
    void shouldCompareWithThreshold() {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage zero = builder.setCovered(0).setMissed(2).build();
        Coverage fifty = builder.setCovered(2).setMissed(2).build();
        Coverage hundred = builder.setCovered(2).setMissed(0).build();

        assertThat(zero.isOutOfValidRange(0)).isFalse();
        assertThat(zero.isOutOfValidRange(0.1)).isTrue();
        assertThat(fifty.isOutOfValidRange(50)).isFalse();
        assertThat(fifty.isOutOfValidRange(50.1)).isTrue();
        assertThat(hundred.isOutOfValidRange(100)).isFalse();
        assertThat(hundred.isOutOfValidRange(100.1)).isTrue();
    }

    private double getDelta(final String value) {
        return Fraction.getFraction(value).doubleValue();
    }

    @Test
    void shouldComputeMaximum() {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage worse = builder.setCovered(0).setMissed(2).build();
        Coverage coverage = builder.setCovered(1).setMissed(1).build();
        Coverage better = builder.setCovered(2).setMissed(0).build();

        assertThat(coverage.max(coverage)).isSameAs(coverage);
        assertThat(coverage.max(better)).isSameAs(better);
        assertThat(coverage.max(worse)).isSameAs(coverage);
    }

    @ParameterizedTest(name = "{index} => Detection of invalid covered items: {0}")
    @ValueSource(ints = {0, 1, 2, 4, 5})
    @DisplayName("Ensure that exception is thrown if totals do not match")
    void shouldThrowExceptionWhenMaximumIsInvalid(final int covered) {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage coverage = builder.setCovered(2).setMissed(1).build();

        assertThatExceptionOfType(AssertionError.class).isThrownBy(
                () -> coverage.max(
                        builder.setCovered(covered).setMissed(0).build()));
    }

    @Test
    @DisplayName("Ensure that exception is thrown if constructor is invoked with invalid values")
    void shouldThrowExceptionWhenInitializationIsInvalid() {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> builder.setCovered(-1).setMissed(0).build());
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> builder.setCovered(0).setMissed(-1).build());
    }

    @Test
    @DisplayName("Ensure that exception is thrown if Value instances are not compatible")
    void shouldThrowExceptionWithIncompatibleValue() {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage coverage = builder.setCovered(1).setMissed(2).build();
        var loc = new LinesOfCode(1);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> coverage.add(loc));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> coverage.max(loc));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> coverage.delta(loc));
    }

    @Test
    void shouldProvideNullObject() {
        assertThat(NO_COVERAGE)
                .hasCovered(0)
                .hasMissed(0)
                .hasTotal(0)
                .hasCoveredPercentage(Percentage.ZERO)
                .hasToString("LINE: n/a");
        assertThat(NO_COVERAGE.add(NO_COVERAGE)).isEqualTo(NO_COVERAGE);
    }

    @Test
    void shouldCreatePercentages() {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage coverage = builder.setCovered(6).setMissed(4).build();
        assertThat(coverage)
                .hasCovered(6)
                .hasMissed(4)
                .hasTotal(10)
                .hasCoveredPercentage(Percentage.valueOf(6, 10))
                .hasToString("LINE: 60.00% (6/10)");

        assertThat(coverage.add(NO_COVERAGE)).isEqualTo(coverage);

        Coverage sum = coverage.add(builder.setCovered(10).setMissed(0).build());
        assertThat(sum).isEqualTo(builder.setCovered(16).setMissed(4).build());
    }

    @ParameterizedTest(name = "Test {index}: Covered items ''{0}''")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    @DisplayName("Coverage creation")
    void shouldCreateCoverage(final int covered) {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage coverage = builder
                .setCovered(covered)
                .setMissed(5 - covered)
                .build();

        assertThat(coverage).hasCovered(covered).hasTotal(5);
        assertThat(coverage.toString()).contains(covered + "/");
    }

    @ParameterizedTest(name = "Test {index}: Covered ''{0}'', Missed ''{1}'', toString ''({2})'''")
    @CsvSource({
            "0, 1, 0/1",
            "1, 0, 1/1",
            "0, 2, 0/2",
            "1, 1, 1/2",
            "2, 0, 2/2"
    })
    @DisplayName("Coverage creation")
    void shouldCreateCoverage(final int covered, final int missed, final String toString) {
        var builder = new CoverageBuilder().setMetric(Metric.LINE);

        Coverage coverage = builder.setCovered(covered).setMissed(missed).build();

        assertThat(coverage).hasCovered(covered).hasMissed(missed);
        assertThat(coverage.toString()).endsWith(String.format("(%s)", toString));
    }

    @Test
    void shouldAdhereToEquals() {
        EqualsVerifier.forClass(Coverage.class).withRedefinedSuperclass().verify();
    }
}
