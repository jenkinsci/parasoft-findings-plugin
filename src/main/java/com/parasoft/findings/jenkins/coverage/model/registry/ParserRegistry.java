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
        }
        throw new IllegalArgumentException("Unknown parser type: " + parser);
    }
}
