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

import static com.parasoft.findings.jenkins.coverage.Assertions.*;

class ContainerNodeTest extends AbstractNodeTest {
    @Override
    Metric getMetric() {
        return Metric.CONTAINER;
    }

    @Override
    Node createNode(final String name) {
        return new ContainerNode(name);
    }

    @Test
    void shouldAggregateSourceFolders() {
        var root = new ContainerNode("root");
        var left = new ModuleNode("left");
        root.addChild(left);
        var right = new ModuleNode("right");
        root.addChild(right);

        assertThat(root.getSourceFolders()).isEmpty();
        assertThat(root.getChildren()).containsExactly(left, right);

        left.addSource("left/path");
        right.addSource("right/path");
        assertThat(root.getSourceFolders()).containsExactly("left/path", "right/path");
    }
}
