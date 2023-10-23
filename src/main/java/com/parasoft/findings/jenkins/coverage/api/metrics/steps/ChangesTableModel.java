/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
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

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.parasoft.findings.jenkins.coverage.model.FileNode;
import com.parasoft.findings.jenkins.coverage.model.Node;

import hudson.Functions;

import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;

/**
 * A base class for coverage table models that handle the changes to a result of a reference build.
 */
abstract class ChangesTableModel extends CoverageTableModel {
    private final Node changeRoot;

    ChangesTableModel(final String id, final Node root, final Node changeRoot,
            final RowRenderer renderer, final ColorProvider colorProvider) {
        super(id, root, renderer, colorProvider);

        this.changeRoot = changeRoot;
    }

    @Override
    public List<Object> getRows() {
        Locale browserLocale = Functions.getCurrentLocale();
        return changeRoot.getAllFileNodes().stream()
                .map(file -> createRow(file, browserLocale))
                .collect(Collectors.toList());
    }

    abstract CoverageRow createRow(FileNode file, Locale browserLocale);

    FileNode getOriginalNode(final FileNode fileNode) {
        return getRoot().getAllFileNodes().stream()
                .filter(node -> node.getRelativePath().equals(fileNode.getRelativePath())
                        && node.getName().equals(fileNode.getName()))
                .findFirst()
                .orElse(fileNode); // return this as fallback to prevent exceptions
    }

    /**
     * UI row model for the changes rows of a table.
     */
    static class ChangesRow extends CoverageRow {
        private final FileNode originalFile;

        ChangesRow(final FileNode originalFile, final FileNode changedFileNode,
                final Locale browserLocale, final RowRenderer renderer, final ColorProvider colorProvider) {
            super(changedFileNode, browserLocale, renderer, colorProvider);

            this.originalFile = originalFile;
        }

        FileNode getOriginalFile() {
            return originalFile;
        }
    }
}
