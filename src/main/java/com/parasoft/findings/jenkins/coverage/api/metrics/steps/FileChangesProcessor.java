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

    /**
     * Attaches the delta between the total file coverage of all currently built files against the passed reference. The
     * algorithm also covers renamed files.
     *
     * @param root
     *         The root of the coverage tree
     * @param referenceNode
     *         The root of the reference coverage tree
     * @param oldPathMapping
     *         A mapping between the report paths of the current and the reference coverage tree
     */
    public void attachFileCoverageDeltas(final Node root, final Node referenceNode,
            final Map<String, String> oldPathMapping) {
        Map<String, FileNode> fileNodes = getFileNodeMappingWithReferencePaths(root, oldPathMapping);
        Map<String, FileNode> referenceFileNodes = getReferenceFileNodeMapping(fileNodes, referenceNode);
        fileNodes.entrySet().stream()
                .filter(entry -> referenceFileNodes.containsKey(entry.getKey()))
                .forEach(entry -> attachFileCoverageDelta(entry.getValue(), referenceFileNodes.get(entry.getKey())));
    }

    /**
     * Attaches the delta between the total coverage of a file against the same file from the reference build.
     *
     * @param fileNode
     *         The {@link FileNode node} which represents the total coverage of a file
     * @param referenceNode
     *         The {@link FileNode reference node} which represents the coverage of the reference file
     */
    private void attachFileCoverageDelta(final FileNode fileNode, final FileNode referenceNode) {
        fileNode.computeDelta(referenceNode);
    }

    /**
     * Gets all {@link FileNode file nodes} from the currently running build which also exist within the
     * reference build and maps them by their fully qualified name from the reference.
     *
     * @param root
     *         the root node of the coverage tree of the currently running build
     * @param oldPathMapping
     *         a mapping between the report fully qualified names of the current and the reference coverage tree
     *
     * @return the created node mapping whose keys are fully qualified names from the reference and which values are the
     *         corresponding nodes from the actual build
     */
    private Map<String, FileNode> getFileNodeMappingWithReferencePaths(
            final Node root, final Map<String, String> oldPathMapping) {
        return root.getAllFileNodes().stream()
                .filter(node -> oldPathMapping.containsKey(node.getRelativePath()))
                .collect(Collectors.toMap(node -> oldPathMapping.get(node.getRelativePath()), Function.identity()));
    }

    /**
     * Gets all {@link FileNode file nodes} from a reference coverage tree which also exist in the current
     * coverage tree. The found nodes are mapped by their path.
     *
     * @param nodeMapping
     *         The file nodes of the current coverage tree, mapped by their paths
     * @param referenceNode
     *         The root of the reference coverage tree
     *
     * @return the created node mapping
     */
    private Map<String, FileNode> getReferenceFileNodeMapping(
            final Map<String, FileNode> nodeMapping, final Node referenceNode) {
        return referenceNode.getAllFileNodes().stream()
                .filter(reference -> nodeMapping.containsKey(reference.getRelativePath()))
                .collect(Collectors.toMap(FileNode::getRelativePath, Function.identity()));
    }
}
