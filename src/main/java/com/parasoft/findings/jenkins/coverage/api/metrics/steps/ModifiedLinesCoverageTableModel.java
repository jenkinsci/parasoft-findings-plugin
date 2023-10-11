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

import java.util.Locale;

import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.Node;

import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;

/**
 * A coverage table model that handles the modified lines of a change with respect to a result of a reference build.
 */
class ModifiedLinesCoverageTableModel extends ChangesTableModel {
    ModifiedLinesCoverageTableModel(final String id, final Node root, final Node changeRoot,
            final RowRenderer renderer, final ColorProvider colorProvider) {
        super(id, root, changeRoot, renderer, colorProvider);
    }

    @Override
    ModifiedLinesCoverageRow createRow(final FileNode file, final Locale browserLocale) {
        return new ModifiedLinesCoverageRow(getOriginalNode(file), file,
                browserLocale, getRenderer(), getColorProvider());
    }

    /**
     * UI row model for the coverage details table of modified lines.
     */
    private static class ModifiedLinesCoverageRow extends ChangesRow {
        ModifiedLinesCoverageRow(final FileNode originalFile, final FileNode changedFileNode,
                final Locale browserLocale, final RowRenderer renderer, final ColorProvider colorProvider) {
            super(originalFile, changedFileNode, browserLocale, renderer, colorProvider);
        }

        @Override
        public int getLoc() {
            return getFile().getCoveredAndModifiedLines().size();
        }
    }
}
