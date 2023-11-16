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

import edu.hm.hafner.util.TreeString;

/**
 * A {@link Node} for a specific package. It converts a package structure to a corresponding path structure.
 *
 * @author Ullrich Hafner
 */
public final class PackageNode extends Node {
    private static final long serialVersionUID = 8236436628673022634L;

    /**
     * Creates a new coverage item node with the given name.
     *
     * @param name
     *         the human-readable name of the node
     */
    public PackageNode(final String name) {
        super(Metric.PACKAGE, name);
    }

    static PackageNode appendPackage(final PackageNode localChild, final PackageNode localParent) {
        localParent.addChild(localChild);
        return localParent;
    }

    @Override
    public PackageNode copy() {
        return new PackageNode(getName());
    }

    /**
     * Create a new file node with the given name and add it to the list of children.
     *
     * @param fileName
     *         the file name
     * @param relativePath
     *         the relative path of the file
     *
     * @return the created and linked file node
     */
    public FileNode createFileNode(final String fileName, final TreeString relativePath) {
        var fileNode = new FileNode(fileName, relativePath);
        addChild(fileNode);
        return fileNode;
    }

    /**
     * Searches for the specified file node. If the file node is not found then a new file node will be created and
     * linked to this package node.
     *
     * @param fileName
     *         the file name
     * @param relativePath
     *         the relative path of the file
     *
     * @return the existing or created file node
     * @see #createFileNode(String, TreeString)
     */
    public FileNode findOrCreateFileNode(final String fileName, final TreeString relativePath) {
        return findFile(fileName).orElseGet(() -> createFileNode(fileName, relativePath));
    }

    /**
     * Searches for the specified class node. If the class node is not found then a new class node will be created and
     * linked to this file node.
     *
     * @param className
     *         the class name
     *
     * @return the created and linked class node
     * @see #createClassNode(String)
     */
    public ClassNode findOrCreateClassNode(final String className) {
        return findClass(className).orElseGet(() -> createClassNode(className));
    }

    /**
     * Create a new class node with the given name and add it to the list of children.
     *
     * @param className
     *         the class name
     *
     * @return the created and linked class node
     */
    public ClassNode createClassNode(final String className) {
        var classNode = new ClassNode(className);
        addChild(classNode);
        return classNode;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s <%d>", getMetric(), getName(), getChildren().size());
    }
}
