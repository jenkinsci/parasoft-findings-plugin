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

import java.io.Serializable;

import com.parasoft.findings.jenkins.coverage.model.CoverageParser;
import com.parasoft.findings.jenkins.coverage.model.Node;
import com.parasoft.findings.jenkins.coverage.model.registry.ParserRegistry;

import org.jvnet.localizer.Localizable;
import hudson.model.AbstractDescribableImpl;

/**
 * A coverage tool that can produce a {@link Node coverage tree} by parsing a given report file.
 *
 * @author Ullrich Hafner
 */
public class CoverageTool extends AbstractDescribableImpl<CoverageTool> implements Serializable { // parasoft-suppress OWASP2021.A8.SCBNP "Using default serialization mechanism."
    private static final long serialVersionUID = -8612521458890553037L;

    /**
     * Supported coverage parsers.
     */
    public enum Parser {
        COBERTURA(Messages._Parser_Cobertura(), "**/cobertura.xml",
                "symbol-footsteps-outline plugin-ionicons-api");

        private final Localizable displayName;
        private final String defaultPattern;
        private final String icon;

        Parser(final Localizable displayName, final String defaultPattern,
                final String icon) {
            this.displayName = displayName;
            this.defaultPattern = defaultPattern;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName.toString();
        }

        public String getDefaultPattern() {
            return defaultPattern;
        }

        public String getIcon() {
            return icon;
        }

        /**
         * Creates a new parser to read the report XML files into a Java object model of {@link Node} instances.
         *
         * @return the parser
         */
        public CoverageParser createParser() {
            return new ParserRegistry().getParser(name());
        }
    }
}
