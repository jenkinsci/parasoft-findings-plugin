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

import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTool.Parser;
import com.parasoft.findings.jenkins.coverage.model.CoverageParser;
import com.parasoft.findings.jenkins.coverage.model.ModuleNode;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;
import io.jenkins.plugins.util.AgentFileVisitor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Scans the workspace for coverage reports that match a specified Ant file pattern and parse these files with the
 * specified parser. Creates a new {@link ModuleNode} for each parsed file. For files that cannot be read, an empty
 * module node will be returned.
 *
 * @author Ullrich Hafner
 */
public class CoverageReportScanner extends AgentFileVisitor<ModuleNode> { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
    private static final long serialVersionUID = 6940864958150044554L;

    private static final PathUtil PATH_UTIL = new PathUtil();
    private final Parser parser;

    /**
     * Creates a new instance of {@link CoverageReportScanner}.
     *
     * @param filePattern
     *         ant file-set pattern to scan for files to parse
     * @param encoding
     *         encoding of the files to parse
     * @param followSymbolicLinks
     *         if the scanner should traverse symbolic links
     * @param parser
     *         the parser to use
     */
    public CoverageReportScanner(final String filePattern, final String encoding,
            final boolean followSymbolicLinks, final Parser parser) {
        super(filePattern, encoding, followSymbolicLinks, true);

        this.parser = parser;
    }

    @Override
    protected Optional<ModuleNode> processFile(final Path file, final Charset charset, final FilteredLog log) {
        try {
            CoverageParser xmlParser = parser.createParser();
            ModuleNode node = xmlParser.parse(Files.newBufferedReader(file, charset), log);
            log.logInfo("Successfully parsed intermediate Cobertura coverage report file '%s'", PATH_UTIL.getAbsolutePath(file));
            node.aggregateValues().forEach(v -> log.logInfo("%s", v));
            return Optional.of(node);
        } catch (Exception exception) { // parasoft-suppress OWASP2021.A5.NCE "This is expected. Reason: Do not fail the build when processing coverage reports."
            log.logError("Parsing of intermediate Cobertura coverage report file '%s' failed due to an exception: %s",
                    file, ExceptionUtils.getRootCauseMessage(exception));
            return Optional.empty();
        }
    }
}
