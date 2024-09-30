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

/**
 * A {@link Node} for a specific method.
 *
 * @author Florian Orendi
 */
public final class MethodNode extends Node { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
    private static final long serialVersionUID = -5765205034179396434L;

    private final String signature;
    /** The line number where the code of method begins (not including the method head). */
    private final int lineNumber;

    /**
     * Creates a new method node with the given name. The line number will be set to 0.
     *
     * @param name
     *         The human-readable name of the node
     * @param signature
     *         The signature of the method
     */
    public MethodNode(final String name, final String signature) {
        this(name, signature, 0);
    }

    /**
     * Creates a new item node with the given name.
     *
     * @param name
     *         The human-readable name of the node
     * @param signature
     *         The signature of the method
     * @param lineNumber
     *         The line number where the method begins (not including the method head)
     */
    public MethodNode(final String name, final String signature, final int lineNumber) {
        super(Metric.METHOD, name);

        this.signature = signature;
        this.lineNumber = lineNumber;
    }

    @Override
    public Node copy() {
        return new MethodNode(getName(), getSignature(), getLineNumber());
    }

    /**
     * Checks whether the line number is valid.
     *
     * @return {@code true} if the line number is valid, else {@code false}
     */
    public boolean hasValidLineNumber() {
        return lineNumber > 0;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSignature() {
        return signature;
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
        MethodNode that = (MethodNode) o;
        return lineNumber == that.lineNumber
                && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), signature, lineNumber);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s(%d) <%s>", getMetric(), getName(), getLineNumber(), getSignature());
    }
}
