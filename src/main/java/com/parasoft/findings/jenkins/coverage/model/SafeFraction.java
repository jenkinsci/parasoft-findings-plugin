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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.math.Fraction;

import static com.parasoft.findings.jenkins.coverage.model.Percentage.*;

/**
 * A small wrapper for {@link Fraction} instances that avoids an arithmetic overflow by using double based operations in
 * case of an exception.
 *
 * @author Ullrich Hafner
 */
public class SafeFraction {
    private final Fraction fraction;

    /**
     * Creates a new fraction instance that wraps the specified fraction with safe operations.
     *
     * @param fraction
     *         the fraction to wrap
     */
    public SafeFraction(final Fraction fraction) {
        this.fraction = fraction;
    }

    /**
     * Creates a new fraction instance that wraps the specified fraction with safe operations.
     *
     * @param numerator
     *         the numerator of the fraction
     * @param denominator
     *         the denominator of the fraction
     */
    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "expected")
    public SafeFraction(final int numerator, final int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException(TOTALS_ZERO_MESSAGE);
        }
        this.fraction = Fraction.getFraction(numerator, denominator);
    }

    /**
     * Multiplies the value of this fraction by another, returning the result in reduced form. Since there might be an
     * arithmetic exception due to an overflow, this method will handle this situation by calculating the multiplication
     * based on the double values of the fractions.
     *
     * @param multiplier
     *         the fraction to multiply by
     *
     * @return a {@code Fraction} instance with the resulting values
     */
    public Fraction multiplyBy(final Fraction multiplier) {
        try {
            return fraction.multiplyBy(multiplier);
        }
        catch (ArithmeticException exception) { // parasoft-suppress OWASP2021.A9.LGE "This is intentionally designed to ensure exceptions during arithmetic processing don't cause the build to fail."
            return Fraction.getFraction(fraction.doubleValue() * multiplier.doubleValue());
        }
    }

    /**
     * Subtracts the value of another fraction from the value of this one, returning the result in reduced form.  Since
     * there might be an arithmetic exception due to an overflow, this method will handle this situation by calculating
     * the subtraction based on the double values of the fractions.
     *
     * @param subtrahend
     *         the fraction to subtract
     *
     * @return a {@code Fraction} instance with the resulting values
     */
    public Fraction subtract(final Fraction subtrahend) {
        try {
            return fraction.subtract(subtrahend);
        }
        catch (ArithmeticException exception) { // parasoft-suppress OWASP2021.A9.LGE "This is intentionally designed to ensure exceptions during arithmetic processing don't cause the build to fail."
            return Fraction.getFraction(fraction.doubleValue() - subtrahend.doubleValue());
        }
    }

    /**
     * Subtracts the value of another fraction from the value of this one, returning the result in reduced form.  Since
     * there might be an arithmetic exception due to an overflow, this method will handle this situation by calculating
     * the subtraction based on the double values of the fractions.
     *
     * @param numerator
     *         the numerator of the fraction
     * @param denominator
     *         the denominator of the fraction
     *
     * @return a {@code Fraction} instance with the resulting values
     */
    public Fraction subtract(final int numerator, final int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException(TOTALS_ZERO_MESSAGE);
        }

        return subtract(Fraction.getFraction(numerator, denominator));
    }

    /**
     * Adds the value of another fraction to the value of this one, returning the result in reduced form.  Since
     * there might be an arithmetic exception due to an overflow, this method will handle this situation by calculating
     * the addition based on the double values of the fractions.
     *
     * @param summand
     *         the fraction to add
     *
     * @return a {@code Fraction} instance with the resulting values
     */
    public Fraction add(final Fraction summand) {
        try {
            return fraction.add(summand);
        }
        catch (ArithmeticException exception) { // parasoft-suppress OWASP2021.A9.LGE "This is intentionally designed to ensure exceptions during arithmetic processing don't cause the build to fail."
            return Fraction.getFraction(fraction.doubleValue() + summand.doubleValue());
        }
    }
}
