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

package com.parasoft.findings.jenkins.coverage.api.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junitpioneer.jupiter.DefaultLocale;

import com.parasoft.findings.jenkins.coverage.model.Coverage.CoverageBuilder;
import com.parasoft.findings.jenkins.coverage.model.CoverageParser;
import com.parasoft.findings.jenkins.coverage.model.CyclomaticComplexity;
import com.parasoft.findings.jenkins.coverage.model.LinesOfCode;
import com.parasoft.findings.jenkins.coverage.model.Metric;
import com.parasoft.findings.jenkins.coverage.model.Node;
import com.parasoft.findings.jenkins.coverage.model.Value;
import com.parasoft.findings.jenkins.coverage.model.parser.JacocoParser;
import com.parasoft.findings.jenkins.coverage.model.parser.CoberturaParser;
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

    /**
     * Creates coverage statistics that can be used in test cases.
     *
     * @return the coverage statistics
     */
    public static CoverageStatistics createStatistics() {
        return new CoverageStatistics(fillValues(), fillValues());
    }

    /**
     * Creates coverage statistics that can be used in test cases.
     *
     * @return the coverage statistics
     */
    public static CoverageStatistics createOnlyProjectStatistics() {
        return new CoverageStatistics(fillValues(), List.of());
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

}
