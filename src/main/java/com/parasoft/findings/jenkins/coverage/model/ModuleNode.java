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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A {@link Node} which represents a module of a project.
 *
 * @author Melissa Bauer
 */
public final class ModuleNode extends Node { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
    private static final long serialVersionUID = 2393265115219226404L;

    private final List<String> sources = new ArrayList<>();

    /**
     * Creates a new module node with the given name.
     *
     * @param name
     *         the name of the module
     */
    public ModuleNode(final String name) {
        super(Metric.MODULE, name);
    }

    @Override
    public ModuleNode copy() {
        var moduleNode = new ModuleNode(getName());
        getSourceFolders().forEach(moduleNode::addSource);
        return moduleNode;
    }

    @Override
    public Set<String> getSourceFolders() {
        return new HashSet<>(sources);
    }

    /**
     * Appends the specified source to the list of sources.
     *
     * @param source
     *         the source to add
     */
    public void addSource(final String source) {
        sources.add(source);
    }

    /**
     * Splits flat packages into a package hierarchy. Changes the internal tree structure of package nodes in place.
     * <p>
     * Examples:
     * </p>
     * <ul>
     *     <li>
     *         A package name {@code "edu"} will produce a single node with the name {@code "edu"}.
     *     </li>
     *     <li>
     *         A package name {@code "edu.hm.hafner"} will produce three package nodes, that are linked together,
     *         starting with the {@code "edu"} package ({@code "edu" -> "hm" -> "hafner"}).
     *     </li>
     * </ul>
     */
    public void splitPackages() {
        var allPackages = getChildren().stream()
                .filter(child -> child.getMetric().equals(Metric.PACKAGE))
                .collect(Collectors.toList());
        allPackages.forEach(this::removeChild);
        for (Node packageNode : allPackages) {
            String[] packageParts = StringUtils.split(packageNode.getName(), "./\\");
            if (packageParts.length > 1) {
                ArrayUtils.reverse(packageParts);
                Optional<PackageNode> splitPackages = Arrays.stream(packageParts)
                        .map(subPackage -> createPackageNode(subPackage, packageNode.getValues()))
                        .reduce(PackageNode::appendPackage);
                PackageNode localRoot = splitPackages.get();
                Node localTail = localRoot;
                while (localTail.hasChildren()) {
                    localTail = localTail.getChildren().get(0);
                }
                localTail.addAllChildren(packageNode.getChildren()); // move the children to the new tail
                mergeSinglePackage(localRoot);
            }
            else {
                mergeSinglePackage(packageNode);
            }
        }
    }

    private void mergeSinglePackage(final Node packageNode) {
        for (Node existing : getChildren()) {
            if (isEqual(packageNode, existing)) {
                // replace existing with merged two nodes
                removeChild(existing);
                Node merged = existing.merge(packageNode);
                addChild(merged);

                return;
            }
        }

        addChild(packageNode); // fallback: if the package does not yet exist add it as new package node
    }

    private static boolean isEqual(final Node packageNode, final Node existing) {
        return existing.getMetric().equals(packageNode.getMetric())
                && existing.getName().equals(packageNode.getName());
    }

    private PackageNode createPackageNode(final String subPackage, final List<Value> existingValues) {
        var packageNode = new PackageNode(subPackage);
        packageNode.addAllValues(existingValues);
        return packageNode;
    }

    /**
     * Create a new package node with the given name and add it to the list of children.
     *
     * @param packageName
     *         the package name
     *
     * @return the created and linked package node
     */
    public PackageNode createPackageNode(final String packageName) {
        var packageNode = new PackageNode(packageName);
        addChild(packageNode);
        return packageNode;
    }

    /**
     * Searches for the specified package node. If the package node is not found then a new package node will be created
     * and linked to this module node.
     *
     * @param packageName
     *         the package name
     *
     * @return the existing or created package node
     * @see #createPackageNode(String)
     */
    public PackageNode findOrCreatePackageNode(final String packageName) {
        return findPackage(packageName).orElseGet(() -> createPackageNode(packageName));
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
        ModuleNode that = (ModuleNode) o;
        return Objects.equals(sources, that.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sources);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s <%d> %s", getMetric(), getName(), getChildren().size(), getSourceFolders());
    }
}
