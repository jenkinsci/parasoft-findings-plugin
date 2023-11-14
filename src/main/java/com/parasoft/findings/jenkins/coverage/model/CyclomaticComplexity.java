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

/**
 * Represents the cyclomatic complexity in a particular code block.
 *
 * @author Melissa Bauer
 */
public final class CyclomaticComplexity extends IntegerValue {
    private static final long serialVersionUID = -1626223071392791727L;

    /**
     * Creates a new {@link CyclomaticComplexity} instance with the specified complexity.
     * The metric is set to {@link Metric#COMPLEXITY}.
     *
     * @param complexity
     *         the cyclomatic complexity
     */
    public CyclomaticComplexity(final int complexity) {
        this(complexity, Metric.COMPLEXITY);
    }

    /**
     * Creates a new {@link CyclomaticComplexity} instance with the specified complexity.
     *
     * @param complexity
     *         the cyclomatic complexity
     * @param metric
     *         the metric of this value
     */
    public CyclomaticComplexity(final int complexity, final Metric metric) {
        super(metric, complexity);
    }

    @Override
    protected IntegerValue create(final int value) {
        return new CyclomaticComplexity(value);
    }
}
