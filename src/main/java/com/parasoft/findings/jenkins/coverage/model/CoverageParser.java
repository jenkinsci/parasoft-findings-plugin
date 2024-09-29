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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.SecureXmlParserFactory.ParsingException;
import edu.hm.hafner.util.TreeStringBuilder;

/**
 * Parses a file and returns the code coverage information in a tree of {@link Node} instances.
 *
 * @author Ullrich Hafner
 */
public abstract class CoverageParser implements Serializable {
    private static final long serialVersionUID = 3941742254762282096L;
    private transient TreeStringBuilder treeStringBuilder = new TreeStringBuilder();

    /**
     * Parses a report provided by the given reader.
     *
     * @param reader
     *         the reader with the coverage information
     * @param log
     *         the logger to write messages to
     *
     * @return the root of the created tree
     * @throws ParsingException
     *         if the XML content cannot be read
     */
    public ModuleNode parse(final Reader reader, final FilteredLog log) {
        var moduleNode = parseReport(reader, log);
        getTreeStringBuilder().dedup();
        return moduleNode;
    }

    /**
     * Called after de-serialization to restore transient fields.
     *
     * @return this
     */
    @SuppressWarnings("PMD.NullAssignment")
    protected Object readResolve() {
        treeStringBuilder = new TreeStringBuilder();

        return this;
    }

    public final TreeStringBuilder getTreeStringBuilder() {
        return treeStringBuilder;
    }

    /**
     * Parses a report provided by the given reader.
     *
     * @param reader
     *         the reader with the coverage information
     * @param log
     *         the logger to write messages to
     *
     * @return the root of the created tree
     * @throws ParsingException
     *         if the XML content cannot be read
     */
    protected abstract ModuleNode parseReport(Reader reader, FilteredLog log);

    protected static Optional<String> getOptionalValueOf(final StartElement element, final QName attribute) {
        Attribute value = element.getAttributeByName(attribute);
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(value.getValue());
    }

    protected static int getIntegerValueOf(final StartElement element, final QName attributeName) {
        try {
            return parseInteger(getValueOf(element, attributeName));
        }
        catch (NumberFormatException ignore) {
            return 0;
        }
    }

    protected static String getValueOf(final StartElement element, final QName attribute) {
        return getOptionalValueOf(element, attribute).orElseThrow(
                () -> new NoSuchElementException(String.format(
                        "Could not obtain attribute '%s' from element '%s'", attribute, element)));
    }

    protected static int parseInteger(final String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ignore) {
            return 0;
        }
    }

    protected static ParsingException createEofException() {
        return new ParsingException("Unexpected end of file");
    }

    private void readObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        // It works exactly as it would without the custom readObject() method.
        in.defaultReadObject();
    }
}
