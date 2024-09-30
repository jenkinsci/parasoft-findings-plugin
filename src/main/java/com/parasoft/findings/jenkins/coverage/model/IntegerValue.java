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
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.math.Fraction;

/**
 * Represents the value of an integer based metric.
 *
 * @author Melissa Bauer
 */
public abstract class IntegerValue extends Value { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
    private static final long serialVersionUID = -1626223071392791727L;

    private final int integer;

    /**
     * Creates a new {@link IntegerValue} with the specified value.
     *
     * @param metric
     *         the metric for this value
     * @param integer
     *         the value to store
     */
    IntegerValue(final Metric metric, final int integer) {
        super(metric);

        this.integer = integer;
    }

    /**
     * Returns the value of this metric.
     *
     * @return the integer value
     */
    public int getValue() {
        return integer;
    }

    @Override
    public IntegerValue add(final Value other) {
        return castAndMap(other, o -> create(integer + o.getValue()));
    }

    protected abstract IntegerValue create(int value);

    @Override
    public IntegerValue max(final Value other) {
        return castAndMap(other, this::computeMax);
    }

    @Override
    public Fraction delta(final Value other) {
        if (hasSameMetric(other) && other instanceof IntegerValue) {
            return Fraction.getFraction(getValue() - ((IntegerValue) other).getValue(), 1);
        }
        throw new IllegalArgumentException(String.format("Cannot cast incompatible types: %s and %s", this, other));
    }

    private IntegerValue computeMax(final IntegerValue other) {
        if (integer >= other.integer) {
            return this;
        }
        return other;
    }

    private IntegerValue castAndMap(final Value other,
            final UnaryOperator<IntegerValue> mapper) {
        if (other.getClass().equals(getClass())) {
            return mapper.apply((IntegerValue) other);
        }
        throw new IllegalArgumentException(String.format("Cannot cast incompatible types: %s and %s", this, other));
    }

    /**
     * Returns whether this integer value is larger than the given threshold.
     *
     * @param threshold
     *         the threshold
     *
     * @return {@code true}, if this value is larger than the specified threshold
     */
    @Override
    public boolean isOutOfValidRange(final double threshold) {
        return getValue() > threshold;
    }

    @Override
    public String serialize() {
        return String.format("%s: %s", getMetric(), integer);
    }

    @Override
    public String toString() {
        return serialize();
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
        IntegerValue that = (IntegerValue) o;
        return integer == that.integer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), integer);
    }
}
