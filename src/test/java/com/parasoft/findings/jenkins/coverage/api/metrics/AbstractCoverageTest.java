package com.parasoft.findings.jenkins.coverage.api.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang3.math.Fraction;
import org.junitpioneer.jupiter.DefaultLocale;

import edu.hm.hafner.coverage.Coverage.CoverageBuilder;
import edu.hm.hafner.coverage.CoverageParser;
import edu.hm.hafner.coverage.CyclomaticComplexity;
import edu.hm.hafner.coverage.LinesOfCode;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.coverage.Value;
import edu.hm.hafner.coverage.parser.JacocoParser;
import edu.hm.hafner.coverage.parser.CoberturaParser;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.ResourceTest;
import edu.hm.hafner.util.SecureXmlParserFactory.ParsingException;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;

/**
 * Base class for coverage tests that work on real coverage reports.
 *
 * @author Ullrich Hafner
 */
@DefaultLocale("en")
@SuppressWarnings("checkstyle:JavadocVariable")
public abstract class AbstractCoverageTest extends ResourceTest {
    public static final String COBERTURA_CODING_STYLE_FILE = "cobertura-codingstyle.xml";

    public static final String COBERTURA_CODING_STYLE_NO_DATA_FILE = "cobertura-codingstyle-no-data.xml";

    private final FilteredLog log = new FilteredLog("Errors");

    /**
     * Reads and parses a JaCoCo coverage report.
     *
     * @param fileName
     *         the name of the coverage report file
     *
     * @return the parsed coverage tree
     */
    protected Node readJacocoResult(final String fileName) {
        return readResult(fileName, new JacocoParser());
    }

    /**
     * Reads and parses a cobertura coverage report.
     *
     * @param fileName
     *         the name of the coverage report file
     *
     * @return the parsed coverage tree
     */
    protected Node readCoberturaResult(final String fileName) {
        return readResult(fileName, new CoberturaParser());
    }

    /**
     * Reads and parses a coverage report.
     *
     * @param fileName
     *         the name of the coverage report file
     * @param parser
     *         the parser to use
     *
     * @return the parsed coverage tree
     */
    protected Node readResult(final String fileName, final CoverageParser parser) {
        try {
            var node = parser.parse(Files.newBufferedReader(getResourceAsFile(fileName)), log);
            node.splitPackages();
            return node;
        }
        catch (ParsingException | IOException exception) {
            throw new AssertionError(exception);
        }
    }

    protected FilteredLog getLog() {
        return log;
    }

    /**
     * Creates coverage statistics that can be used in test cases.
     *
     * @return the coverage statistics
     */
    public static CoverageStatistics createStatistics() {
        return new CoverageStatistics(fillValues(), fillDeltas(),
                fillValues(), fillDeltas(),
                fillValues(), fillDeltas());
    }

    /**
     * Creates coverage statistics that can be used in test cases.
     *
     * @return the coverage statistics
     */
    public static CoverageStatistics createOnlyProjectStatistics() {
        return new CoverageStatistics(fillValues(),
                new TreeMap<>(), List.of(), new TreeMap<>(), List.of(), new TreeMap<>());
    }

    private static List<Value> fillValues() {
        var builder = new CoverageBuilder();
        return List.of(
                builder.setMetric(Metric.FILE).setCovered(3).setMissed(1).build(),
                builder.setMetric(Metric.LINE).setCovered(2).setMissed(2).build(),
                builder.setMetric(Metric.BRANCH).setCovered(9).setMissed(1).build(),
                new CyclomaticComplexity(150),
                new CyclomaticComplexity(15, Metric.COMPLEXITY_MAXIMUM),
                new LinesOfCode(1000)
        );
    }

    private static NavigableMap<Metric, Fraction> fillDeltas() {
        final NavigableMap<Metric, Fraction> deltaMapping = new TreeMap<>();
        deltaMapping.put(Metric.FILE, Fraction.getFraction(-10, 100));
        deltaMapping.put(Metric.LINE, Fraction.getFraction(5, 100));
        deltaMapping.put(Metric.COMPLEXITY, Fraction.getFraction(-10, 1));
        deltaMapping.put(Metric.LOC, Fraction.getFraction(5, 1));
        return deltaMapping;
    }
}
