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

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for {@link Percentage}.
 *
 * @author Florian Orendi
 */
class PercentageTest {

    private static final double COVERAGE_FRACTION = 0.5;

    @Test
    void shouldHandleOverflow() {
        Fraction fraction = Fraction.getFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        Percentage percentage = Percentage.valueOf(fraction);
        assertThat(percentage.toDouble()).isEqualTo(100);
    }

    @Test
    void shouldCreatePercentageFromFraction() {
        Fraction fraction = Fraction.getFraction(COVERAGE_FRACTION);
        Percentage percentage = Percentage.valueOf(fraction);
        assertThat(percentage.toDouble()).isEqualTo(50.0);
    }

    @Test
    void shouldCreatePercentageFromNumeratorAndDenominator() {
        Percentage percentage = Percentage.valueOf(50, 100);
        assertThat(percentage.toDouble()).isEqualTo(50.0);
    }

    @Test
    void shouldNotCreatePercentageFromNumeratorAndZeroDenominator() {
        assertThatThrownBy(() -> Percentage.valueOf(50, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Percentage.TOTALS_ZERO_MESSAGE);
    }

    @Test
    void shouldNotCreatePercentageOfInvalidStringRepresentation() {
        assertThatThrownBy(() -> Percentage.valueOf("99%"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Percentage.valueOf("0.99/1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.forClass(Percentage.class).verify();
    }

    @Test
    void shouldSerializeInstance() {
        Percentage percentage = Percentage.valueOf(49, 100);
        assertThat(percentage.serializeToString()).isEqualTo("49/100");
        assertThat(Percentage.valueOf("49/100")).isEqualTo(percentage).hasToString("49.00%");

        assertThatIllegalArgumentException().isThrownBy(() -> Percentage.valueOf("1/0"));
        assertThatIllegalArgumentException().isThrownBy(() -> Percentage.valueOf("2/1"));
    }
}
