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
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static com.parasoft.findings.jenkins.coverage.Assertions.*;

/**
 * Tests the class {@link FractionValue}.
 *
 * @author Ullrich Hafner
 */
class FractionValueTest {
    @Test
    void shouldCreateDelta() {
        var fraction = Fraction.getFraction(50, 1);
        var fifty = new FractionValue(Metric.LINE, fraction);
        var fiftyAgain = new FractionValue(Metric.LINE, 50, 1);
        var hundred = new FractionValue(Metric.LINE, Fraction.getFraction(100, 1));

        assertThat(fifty.isOutOfValidRange(50.1)).isTrue();
        assertThat(fifty.isOutOfValidRange(50)).isFalse();

        assertThat(fifty.add(fifty)).isEqualTo(hundred);
        assertThat(fifty.max(hundred)).isEqualTo(hundred);
        assertThat(fifty.max(fiftyAgain)).isEqualTo(fifty);
        assertThat(hundred.max(fifty)).isEqualTo(hundred);

        assertThat(fifty).hasFraction(fraction);
        assertThat(fifty.serialize()).isEqualTo("LINE: 50/1");
    }

    @Test
    void shouldVerifyContract() {
        var fifty = new FractionValue(Metric.LINE, Fraction.getFraction(50, 1));
        var hundred = new FractionValue(Metric.FILE, Fraction.getFraction(100, 1));

        var fiftyLoc = new FractionValue(Metric.LOC, Fraction.getFraction(50, 1));
        var loc = new LinesOfCode(2);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> fifty.add(hundred));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> fiftyLoc.add(loc));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> fifty.delta(hundred));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> fiftyLoc.delta(loc));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> fifty.max(hundred));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> fiftyLoc.max(loc));
    }

    @Test
    void shouldReturnDelta() {
        var fifty = new FractionValue(Metric.LINE, Fraction.getFraction(50, 1));
        var hundred = new FractionValue(Metric.LINE, Fraction.getFraction(100, 1));

        assertThat(fifty.isOutOfValidRange(50.1)).isTrue();
        assertThat(fifty.isOutOfValidRange(50)).isFalse();

        assertThat(hundred.delta(fifty)).isEqualTo(Fraction.getFraction(50, 1));
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.forClass(FractionValue.class).withRedefinedSuperclass().verify();
    }
}
