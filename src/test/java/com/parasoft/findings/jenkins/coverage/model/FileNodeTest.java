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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.TreeString;

import nl.jqno.equalsverifier.Warning;
import nl.jqno.equalsverifier.api.EqualsVerifierApi;

import static com.parasoft.findings.jenkins.coverage.Assertions.*;

class FileNodeTest extends AbstractNodeTest {
    @Override
    Metric getMetric() {
        return Metric.FILE;
    }

    @Override
    void configureEqualsVerifier(final EqualsVerifierApi<? extends Node> verifier) {
        verifier.withPrefabValues(TreeString.class, TreeString.valueOf("src"), TreeString.valueOf("test"))
                .suppress(Warning.NONFINAL_FIELDS);
    }

    @Override
    FileNode createNode(final String name) {
        var fileNode = new FileNode(name, "path");
        fileNode.addCounters(10, 1, 0);
        fileNode.addCounters(11, 2, 2);
        fileNode.addModifiedLines(10);
        fileNode.addIndirectCoverageChange(15, 123);
        var empty = new FileNode("empty", "path");
        fileNode.computeDelta(empty);
        return fileNode;
    }

    @Test
    void shouldGetFilePath() {
        var module = new ModuleNode("top-level"); // just for testing
        var folder = new PackageNode("folder"); // just for testing
        var fileName = "Coverage.java";
        var relativePath = "relative/path/to/file";
        var file = new FileNode(fileName, relativePath);
        folder.addChild(file);
        module.addChild(folder);

        assertThat(file.getRelativePath()).isEqualTo(relativePath);
        var otherPath = "other";

        assertThat(file.getFiles()).containsExactly(relativePath);
        assertThat(folder.getFiles()).containsExactly(relativePath);
        assertThat(module.getFiles()).containsExactly(relativePath);

        assertThat(module.getAll(Metric.FILE)).containsExactly(file);
        assertThat(folder.getAll(Metric.FILE)).containsExactly(file);
        assertThat(file.getAll(Metric.FILE)).containsExactly(file);

        file.setRelativePath(TreeString.valueOf(otherPath));
        assertThat(file.getRelativePath()).isEqualTo(otherPath);

        assertThat(file.matches(Metric.FILE, fileName)).isTrue();
        assertThat(file.matches(Metric.FILE, otherPath)).isTrue();
        assertThat(file.matches(Metric.FILE, "wrong")).isFalse();

        assertThat(file.matches(Metric.FILE, fileName.hashCode())).isTrue();
        assertThat(file.matches(Metric.FILE, otherPath.hashCode())).isTrue();
        assertThat(file.matches(Metric.FILE, "wrong".hashCode())).isFalse();
    }

    @Test
    @Disabled
    void shouldReadOldVersion() {
        byte[] restored = readAllBytes("version-0.21.0.ser");

        var serializable = (FileNode)createSerializable();
        serializable.setRelativePath(TreeString.valueOf(StringUtils.EMPTY));
        assertThatRestoredInstanceEqualsOriginalInstance(serializable, restore(restored));
    }
}
