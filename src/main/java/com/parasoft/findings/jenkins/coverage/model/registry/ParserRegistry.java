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

package com.parasoft.findings.jenkins.coverage.model.registry;

import com.parasoft.findings.jenkins.coverage.model.CoverageParser;
import com.parasoft.findings.jenkins.coverage.model.parser.CoberturaParser;
import com.parasoft.findings.jenkins.coverage.model.parser.JacocoParser;

/**
 * Provides a registry for all available {@link CoverageParserType parsers}.
 *
 * @author Ullrich Hafner
 */
public class ParserRegistry {
    /** Supported parsers. */
    public enum CoverageParserType {
        COBERTURA,
        JACOCO
    }

    /**
     * Returns the parser for the specified name.
     *
     * @param parserName
     *         the name of the parser
     *
     * @return the created parser
     */
    public CoverageParser getParser(final String parserName) {
        return getParser(CoverageParserType.valueOf(parserName));
    }

    /**
     * Returns the parser for the specified name.
     *
     * @param parser
     *         the parser
     *
     * @return the created parser
     */
    public CoverageParser getParser(final CoverageParserType parser) {
        switch (parser) {
            case COBERTURA:
                return new CoberturaParser();
            case JACOCO:
                return new JacocoParser();
            default:
                throw new IllegalArgumentException("Unknown parser type: " + parser);
        }
    }
}
