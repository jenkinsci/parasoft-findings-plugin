/*
 * Copyright 2023 Parasoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parasoft.findings.jenkins.coverage;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;
import io.jenkins.plugins.util.AgentFileVisitor;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.jenkinsci.lib.dtkit.util.converter.ConversionService;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ParasoftCoverageReportScanner extends AgentFileVisitor<ProcessedFileResult> {

    private static final long serialVersionUID = 6940864958150044554L;

    private static final String GENERATED_COVERAGE_DIR = "generatedCoverageFiles";
    private static final String MODIFIED_PARASOFT_REPORT_NAME_SUFFIX = "-processed.xml";
    private static final String GENERATED_COBERTURA_REPORT_NAME_SUFFIX = "-cobertura.xml";
    private static final String COVERAGE_TAG_START = "<Coverage ";
    private static final String WORKING_DIRECTORY_ATTR_FORMAT = "pipelineBuildWorkingDirectory=\"%s\" ";
    private static final String XML_EXTENSION = ".xml";
    private static final String COVERAGE_ATTRIBUTE = "ver";

    private static final PathUtil PATH_UTIL = new PathUtil();

    private final String xslContent;
    private final String workspaceLoc;

    private final Map<String, String> generatedCoverageDirsMap = new HashMap<>();

    public ParasoftCoverageReportScanner(final String filePattern, final String xslContent, final String workspaceLoc,
                                         final String encoding, final boolean followSymbolicLinks) {
        super(filePattern, encoding, followSymbolicLinks, true);

        this.xslContent = xslContent;
        this.workspaceLoc = workspaceLoc;
    }

    @Override
    protected Optional<ProcessedFileResult> processFile(Path file, Charset charset, FilteredLog log) {
        try {
            if (!PATH_UTIL.getAbsolutePath(file).endsWith(XML_EXTENSION)) {
                throw new IOException("Unrecognized report file '" + file + "'");
            }
            Path generatedCoverageBuildDir = createGeneratedCoverageFileDir(file);
            Path processedParasoftReport = generatedCoverageBuildDir.resolve(file.getFileName()
                    + MODIFIED_PARASOFT_REPORT_NAME_SUFFIX);
            processParasoftReport(file, charset, processedParasoftReport);
            Path outputCoberturaReport = generatedCoverageBuildDir.resolve(file.getFileName()
                    + GENERATED_COBERTURA_REPORT_NAME_SUFFIX);
            ConversionService conversionService = new ConversionService();
            conversionService.convert(new StreamSource(new StringReader(xslContent)),
                    processedParasoftReport.toFile(), outputCoberturaReport.toFile());
            log.logInfo("Successfully parsed file '%s'", PATH_UTIL.getAbsolutePath(file));
            return Optional.of(new ProcessedFileResult(PATH_UTIL.getRelativePath(Paths.get(workspaceLoc),
                    outputCoberturaReport), generatedCoverageBuildDir.toString()));
        } catch (IOException | NoSuchElementException | ConversionException exception) {
            log.logException(exception, "Parsing of file '%s' failed due to an exception:", file);
            return Optional.empty();
        }
    }

    private void processParasoftReport(Path originalParasoftReport, Charset charset,
                                       Path processedParasoftReport) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(originalParasoftReport, charset);
             BufferedWriter writer = Files.newBufferedWriter(processedParasoftReport)) {
            boolean attrAdded = false;
            String line;
            while ((line = reader.readLine()) != null) {
                // "COVERAGE_ATTRIBUTE" check is required to differentiate <Coverage> in coverage.xml with <Coverage> inside <Exec> in report.xml
                if (!attrAdded && StringUtils.contains(line, COVERAGE_TAG_START) && StringUtils.contains(line, COVERAGE_ATTRIBUTE)) {
                    writer.append(StringUtils.replaceOnce(line, COVERAGE_TAG_START,
                            COVERAGE_TAG_START + String.format(WORKING_DIRECTORY_ATTR_FORMAT, workspaceLoc)));
                    attrAdded = true;
                } else {
                    writer.append(line);
                }
                writer.newLine();
            }

            if (!attrAdded) {
                throw new NoSuchElementException("No Parasoft coverage information found in the specified file.");
            }
        }
    }

    private Path createGeneratedCoverageFileDir(Path file) throws IOException {
        Path generatedCoverageDir = file.resolveSibling(GENERATED_COVERAGE_DIR);
        Path generatedCoverageBuildDir;
        // If there are multiple Parasoft coverage files in the same directory,
        // the generated coverage files are put in the same subdirectory(use UUID as name) for this build.
        if (generatedCoverageDirsMap.containsKey(generatedCoverageDir.toString())) {
            generatedCoverageBuildDir = Paths.get(generatedCoverageDirsMap.get(generatedCoverageDir.toString()));
        } else {
            generatedCoverageBuildDir = generatedCoverageDir.resolve(UUID.randomUUID().toString());
            generatedCoverageDirsMap.put(generatedCoverageDir.toString(), generatedCoverageBuildDir.toString());
        }
        Files.createDirectories(generatedCoverageBuildDir);
        return generatedCoverageBuildDir;
    }
}
