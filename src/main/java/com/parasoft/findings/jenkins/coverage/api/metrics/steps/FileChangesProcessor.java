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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.Node;

import io.jenkins.plugins.forensics.delta.Change;
import io.jenkins.plugins.forensics.delta.ChangeEditType;
import io.jenkins.plugins.forensics.delta.FileChanges;

/**
 * Calculates and attaches values to the {@link FileNode nodes} of the coverage tree which represent the changes
 * concerning code and coverage.
 *
 * @author Florian Orendi
 */
public class FileChangesProcessor {
    /**
     * Attaches the changed code lines to the file nodes of the coverage tree.
     *
     * @param coverageNode
     *         The root node of the coverage tree
     * @param codeChanges
     *         The code changes to be attached
     */
    public void attachChangedCodeLines(final Node coverageNode, final Map<String, FileChanges> codeChanges) {
        Map<String, FileNode> nodePathMapping = coverageNode.getAllFileNodes().stream()
                .collect(Collectors.toMap(FileNode::getRelativePath, Function.identity()));

        codeChanges.forEach((path, fileChange) -> {
            if (nodePathMapping.containsKey(path)) {
                FileNode changedNode = nodePathMapping.get(path);
                attachChanges(changedNode, fileChange.getChangesByType(ChangeEditType.INSERT));
                attachChanges(changedNode, fileChange.getChangesByType(ChangeEditType.REPLACE));
            }
        });
    }

    /**
     * Attaches a set of changes to a specific {@link FileNode node}.
     *
     * @param changedNode
     *         The node which contains code changes
     * @param relevantChanges
     *         The relevant changes
     */
    private void attachChanges(final FileNode changedNode, final Set<Change> relevantChanges) {
        for (Change change : relevantChanges) {
            for (int i = change.getFromLine(); i <= change.getToLine(); i++) {
                changedNode.addModifiedLines(i);
            }
        }
    }
}
