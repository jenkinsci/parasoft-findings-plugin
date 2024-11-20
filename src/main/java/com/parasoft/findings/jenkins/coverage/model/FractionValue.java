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

import java.util.Objects;

import org.apache.commons.lang3.math.Fraction;

import com.parasoft.findings.jenkins.coverage.model.Metric.MetricTendency;

/**
 * Represents the value of a rational number based metric. Internally the rational number is stored using a
 * {@link Fraction} instance.
 *
 * @author Ullrich Hafner
 */
public final class FractionValue extends Value {
    private static final long serialVersionUID = -7019903979028578410L;

    private final Fraction fraction;

    /**
     * Creates a new {@link FractionValue} with the specified value for the given metric.
     *
     * @param metric
     *         the coverage metric
     * @param fraction
     *         the value to store
     */
    public FractionValue(final Metric metric, final Fraction fraction) {
        super(metric);

        this.fraction = fraction;
    }

    /**
     * Creates a new {@link FractionValue} from the specified counters for the given metric.
     *
     * @param metric
     *         the coverage metric
     * @param numerator
     *         the numerator of the rational number
     * @param denominator
     *         the denominator of the rational number
     */
    public FractionValue(final Metric metric, final int numerator, final int denominator) {
        this(metric, Fraction.getFraction(numerator, denominator));
    }

    public Fraction getFraction() {
        return fraction;
    }

    @Override
    public Value add(final Value other) {
        if (hasSameMetric(other) && other instanceof FractionValue) {
            return new FractionValue(getMetric(), asSafeFraction().add(((FractionValue) other).fraction));
        }
        throw new IllegalArgumentException(String.format("Cannot cast incompatible types: %s and %s", this, other));
    }

    @Override
    public Fraction delta(final Value other) {
        if (hasSameMetric(other) && other instanceof FractionValue) {
            return asSafeFraction().subtract(((FractionValue) other).fraction);
        }
        throw new IllegalArgumentException(String.format("Cannot cast incompatible types: %s and %s", this, other));
    }

    @Override
    public Value max(final Value other) {
        if (hasSameMetric(other) && other instanceof FractionValue) {
            if (fraction.doubleValue() < ((FractionValue) other).fraction.doubleValue()) {
                return other;
            }
            return this;
        }
        throw new IllegalArgumentException(String.format("Cannot cast incompatible types: %s and %s", this, other));
    }

    /**
     * Returns whether this fraction is out of the valid range. For values that have a metric that is getting better
     * when values are increasing (e.g., coverage), the method will return {@code true} if the fraction value is
     * smaller than the threshold. For values that have a metric that is getting better when values are decreasing
     * (e.g., complexity), the method will return {@code true} if the fraction value is larger than the threshold.
     *
     * @param threshold
     *         the threshold to compare with
     *
     * @return {@code true}, if this value is larger or smaller than specified threshold (see {@link MetricTendency})
     */
    @Override
    public boolean isOutOfValidRange(final double threshold) {
        if (getMetric().getTendency() == MetricTendency.LARGER_IS_BETTER) {
            return fraction.doubleValue() < threshold;
        }
        return fraction.doubleValue() > threshold;
    }

    private SafeFraction asSafeFraction() {
        return new SafeFraction(fraction);
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public String serialize() {
        return String.format("%s: %s", getMetric(), fraction);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        FractionValue fractionValue = (FractionValue) o;
        return Objects.equals(fraction, fractionValue.fraction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fraction);
    }
}
