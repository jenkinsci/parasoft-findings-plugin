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

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SafeFraction}.
 *
 * @author Ullrich Hafner
 */
class SafeFractionTest {
    @Test
    void shouldDelegateToFraction() {
        var ten = Fraction.getFraction(10, 1);
        var safeFraction = new SafeFraction(ten);
        assertThat(safeFraction.multiplyBy(ten).doubleValue()).isEqualTo(100.0);
        assertThat(safeFraction.subtract(ten).doubleValue()).isEqualTo(0);
        assertThat(safeFraction.add(ten).doubleValue()).isEqualTo(20.0);
    }

    @Test
    void shouldHandleOverflowForMultiply() {
        var fraction = Fraction.getFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        var safeFraction = new SafeFraction(fraction);
        assertThat(safeFraction.multiplyBy(Fraction.getFraction("100.0")).doubleValue()).isEqualTo(100.0);
    }

    @Test
    void shouldHandleOverflowForSubtract() {
        var fraction = Fraction.getFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        var safeFraction = new SafeFraction(fraction);
        assertThat(safeFraction.subtract(Fraction.getFraction("100.0")).doubleValue()).isEqualTo(-99.0);
    }

    @Test
    void shouldHandleOverflowForAdd() {
        var fraction = Fraction.getFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        var safeFraction = new SafeFraction(fraction);
        assertThat(safeFraction.add(Fraction.getFraction("100.0")).doubleValue()).isEqualTo(101.0);
    }
}
