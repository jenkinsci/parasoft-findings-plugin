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

import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.coverage.model.Coverage.CoverageBuilder;

import nl.jqno.equalsverifier.EqualsVerifier;

import static com.parasoft.findings.jenkins.coverage.Assertions.*;

/**
 * Tests the class {@link CyclomaticComplexity}.
 *
 * @author Melissa Bauer
 */
class CyclomaticComplexityTest {
    private static final Coverage COVERAGE = new CoverageBuilder().setMetric(Metric.LINE)
            .setCovered(1)
            .setMissed(1)
            .build();

    @Test
    void shouldCreateComplexityLeaf() {
        assertThat(new CyclomaticComplexity(125)).hasValue(125);
        assertThat(new CyclomaticComplexity(0)).hasValue(0);
    }

    @Test
    void shouldCompareWithThreshold() {
        assertThat(new CyclomaticComplexity(125).isOutOfValidRange(200)).isFalse();
        assertThat(new CyclomaticComplexity(125).isOutOfValidRange(125)).isFalse();
        assertThat(new CyclomaticComplexity(125).isOutOfValidRange(124.9)).isTrue();
    }

    @Test
    void shouldOperateWithComplexities() {
        assertThat(new CyclomaticComplexity(25).add(new CyclomaticComplexity(100))).hasValue(125);
        assertThat(new CyclomaticComplexity(25).max(new CyclomaticComplexity(100))).hasValue(100);
        assertThat(new CyclomaticComplexity(100).max(new CyclomaticComplexity(99))).hasValue(100);
    }

    @Test
    void shouldFailAddForInvalidTypes() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new CyclomaticComplexity(25).add(new LinesOfCode(100)));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new CyclomaticComplexity(25).add(COVERAGE));
    }

    @Test
    void shouldFailMaxForInvalidTypes() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new CyclomaticComplexity(25).max(new LinesOfCode(100)));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new CyclomaticComplexity(25).max(COVERAGE));
    }

    @Test
    void shouldAdhereToEquals() {
        EqualsVerifier.forClass(CyclomaticComplexity.class).withRedefinedSuperclass().verify();
    }
}
