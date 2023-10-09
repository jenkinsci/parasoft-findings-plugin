package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.io.Serializable;

import edu.hm.hafner.coverage.CoverageParser;
import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.coverage.registry.ParserRegistry;

import org.jvnet.localizer.Localizable;
import hudson.model.AbstractDescribableImpl;

/**
 * A coverage tool that can produce a {@link Node coverage tree} by parsing a given report file.
 *
 * @author Ullrich Hafner
 */
public class CoverageTool extends AbstractDescribableImpl<CoverageTool> implements Serializable {
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
