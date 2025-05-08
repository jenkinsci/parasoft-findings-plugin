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

import static org.assertj.core.api.Assertions.*;

class ClassNodeTest extends AbstractNodeTest {

    @Override
    protected Metric getMetric() {
        return Metric.CLASS;
    }

    @Override
    protected Node createNode(final String name) {
        return new ClassNode(name);
    }

    @Test
    void shouldHandleUnexpectedNodes() {
        var root = new ClassNode("Class");
        var main = new MethodNode("main", "String...");
        root.addChild(main);
        root.addChild(new ClassNode("NestedClass"));

        assertThat(root.findMethod("main", "String..."))
                .isPresent()
                .containsSame(main);
        assertThat(root.findMethod("main", "Nothing"))
                .isNotPresent();
    }
}
