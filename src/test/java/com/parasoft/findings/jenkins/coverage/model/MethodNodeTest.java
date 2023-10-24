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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.parasoft.findings.jenkins.coverage.model.Coverage.CoverageBuilder;

import static com.parasoft.findings.jenkins.coverage.Assertions.*;

class MethodNodeTest extends AbstractNodeTest {
    @Override
    Node createNode(final String name) {
        return new MethodNode(name, "(Ljava/util/Map;)V", 1234);
    }

    @Override
    Metric getMetric() {
        return Metric.METHOD;
    }

    @Test
    void shouldCreateMethodCoverageNode() {
        assertThat(new MethodNode("shouldCreateMethodCoverageNode()", "(Ljava/util/Map;)V", 16))
                .hasMetric(Metric.METHOD)
                .hasName("shouldCreateMethodCoverageNode()")
                .hasSignature("(Ljava/util/Map;)V")
                .hasLineNumber(16)
                .hasValidLineNumber();
    }

    @Test
    void shouldGetValidLineNumber() {
        int validLineNumber = 5;
        var node = new MethodNode("main", "(Ljava/util/Map;)V", validLineNumber);

        assertThat(node)
                .hasValidLineNumber()
                .hasLineNumber(validLineNumber);

        int secondValidLineNumber = 1;
        var secondNode = new MethodNode("main", "(Ljava/util/Map;)V",  secondValidLineNumber);
        assertThat(secondNode)
                .hasValidLineNumber()
                .hasLineNumber(secondValidLineNumber);
    }

    @ParameterizedTest(name = "[{index}] Compute method coverage based on {0} metric")
    @EnumSource(value = Metric.class, names = {"LINE", "BRANCH", "INSTRUCTION"})
    void shouldComputeMethodCoverage(final Metric targetMetric) {
        var node = new MethodNode("method", "signature");

        var builder = new CoverageBuilder().setMetric(Metric.METHOD);
        var notCovered = builder.setCovered(0).setMissed(1).build();
        var covered = builder.setCovered(1).setMissed(0).build();

        assertThat(node.getValue(Metric.METHOD)).isPresent().contains(notCovered);

        node.addValue(builder.setMetric(targetMetric).setCovered(1).setMissed(0).build());
        assertThat(node.getValue(Metric.METHOD)).isPresent().contains(covered);
    }

    @Test
    void shouldCheckInvalidLineNumber() {
        // Given
        var node = new MethodNode("main", "(Ljava/util/Map;)V", -1);
        var secondNode = new MethodNode("main", "(Ljava/util/Map;)V", 0);

        // When & Then
        assertThat(node).doesNotHaveValidLineNumber();
        assertThat(secondNode).doesNotHaveValidLineNumber();
    }

    @Test
    void shouldCheckLineNumberZero() {
        // Given
        var node = new MethodNode("main", "(Ljava/util/Map;)V");

        // When & Then
        assertThat(node).hasMetric(Metric.METHOD).hasLineNumber(0);
    }
}
