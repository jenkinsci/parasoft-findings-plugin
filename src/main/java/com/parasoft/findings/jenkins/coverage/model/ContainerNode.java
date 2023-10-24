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
 * A {@link Node} which represents the top level node in the tree. Typically, such a node aggregates all
 * {@link ModuleNode modules} of a software project.
 *
 * @author Ullrich Hafner
 */
public final class ContainerNode extends Node {
    private static final long serialVersionUID = 6304208788771158650L;

    /**
     * Creates a new {@link ContainerNode} with the given name.
     *
     * @param name
     *         the name of the node
     */
    public ContainerNode(final String name) {
        super(Metric.CONTAINER, name);
    }

    @Override
    public ContainerNode copy() {
        return new ContainerNode(getName());
    }
}
