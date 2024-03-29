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

package com.parasoft.findings.jenkins.coverage.api.metrics.source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.parasoft.findings.jenkins.coverage.model.FileNode;
import edu.hm.hafner.util.FilteredLog;

import hudson.FilePath;
import hudson.model.Run;
import hudson.util.TextFile;

import static com.parasoft.findings.jenkins.coverage.api.metrics.source.CoverageSourcePrinter.TOOLTIP_ATTR;

/**
 * Facade to the source code file structure in Jenkins build folder. Access of those files should be done using an
 * instance of this class only.
 *
 * @author Ullrich Hafner
 * @author Florian Orendi
 */
public class SourceCodeFacade {
    /** Toplevel directory in the build folder of the controller that contains the zipped source files. */
    static final String COVERAGE_SOURCES_DIRECTORY = "coverage-sources";
    static final String COVERAGE_SOURCES_ZIP = "coverage-sources.zip";
    static final int MAX_FILENAME_LENGTH = 245; // Windows has limitations on long file names
    static final String ZIP_FILE_EXTENSION = ".zip";

    static String sanitizeFilename(final String inputName) {
        return StringUtils.right(inputName.replaceAll("[^a-zA-Z0-9-_.]", "_"), MAX_FILENAME_LENGTH);
    }

    /**
     * Reads the contents of the source file of the given file into a String.
     *
     * @param buildResults
     *         Jenkins directory for build results
     * @param id
     *         if of the coverage results
     * @param path
     *         relative path to the coverage node base filename of the coverage node
     *
     * @return the file content as String
     */
    public String read(final File buildResults, final String id, final String path)
            throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory(COVERAGE_SOURCES_DIRECTORY);
        FilePath unzippedSourcesDir = new FilePath(tempDir.toFile());
        try {
            FilePath inputZipFile = new FilePath(createFileInBuildFolder(buildResults, id, path));
            inputZipFile.unzip(unzippedSourcesDir);
            String actualPaintedSourceFileName = StringUtils.removeEnd(sanitizeFilename(path), ZIP_FILE_EXTENSION);
            File sourceFile = tempDir.resolve(actualPaintedSourceFileName).toFile();

            return new TextFile(sourceFile).read();
        }
        finally {
            unzippedSourcesDir.deleteRecursive();
        }
    }

    /**
     * Returns whether the source code is available for the specified source file.
     *
     * @param buildResults
     *         Jenkins directory for build results
     * @param id
     *         if of the coverage results
     * @param path
     *         relative path to the source code filename name
     *
     * @return the file content as String
     */
    public boolean canRead(final File buildResults, final String id, final String path) {
        return createFileInBuildFolder(buildResults, id, path).canRead();
    }

    /**
     * Checks whether any source files has been stored. Even if it is wanted, there might have been errors which cause
     * the absence of any source files.
     *
     * @param buildResults
     *         Jenkins directory for build results
     * @param id
     *         id of the coverage results
     *
     * @return {@code true} whether source files has been stored, else {@code false}
     */
    public boolean hasStoredSourceCode(final File buildResults, final String id) {
        File sourceFolder = new File(buildResults, COVERAGE_SOURCES_DIRECTORY);
        File elementFolder = new File(sourceFolder, id);
        File[] files = elementFolder.listFiles();
        return files != null && files.length > 0;
    }

    String getCoverageSourcesDirectory() {
        return COVERAGE_SOURCES_DIRECTORY;
    }

    /**
     * Copies the zipped source files from the agent to the controller and unpacks them in the coverage-sources folder
     * of the current build.
     *
     * @param build
     *         the build with the coverage result
     * @param workspace
     *         the workspace on the agent that created the ZIP file
     * @param log
     *         the log
     *
     * @throws InterruptedException
     *         in case the user terminated the job
     */
    void copySourcesToBuildFolder(final Run<?, ?> build, final FilePath workspace, final FilteredLog log)
            throws InterruptedException {
        try {
            FilePath buildFolder = new FilePath(build.getRootDir()).child(COVERAGE_SOURCES_DIRECTORY);
            FilePath buildZip = buildFolder.child(COVERAGE_SOURCES_ZIP);
            workspace.child(COVERAGE_SOURCES_ZIP).copyTo(buildZip);
            log.logInfo("-> extracting...");
            buildZip.unzip(buildFolder);
            buildZip.delete();
            log.logInfo("-> done");
        }
        catch (IOException exception) {
            log.logError("Can't copy zipped sources from agent to controller due to an exception: %s", ExceptionUtils.getRootCauseMessage(exception));
        }
    }

    /**
     * Returns a file to a source file in Jenkins' build folder. Note that the file might not exist.
     *
     * @param buildResults
     *         Jenkins directory for build results
     * @param id
     *         if of the coverage results
     * @param path
     *         relative path to the coverage node base filename of the coverage node
     *
     * @return the file
     */
    File createFileInBuildFolder(final File buildResults, final String id, final String path) {
        File sourceFolder = new File(buildResults, COVERAGE_SOURCES_DIRECTORY);
        File elementFolder = new File(sourceFolder, id);

        return new File(elementFolder, sanitizeFilename(path) + ZIP_FILE_EXTENSION);
    }

    /**
     * Filters the sourcecode coverage highlighting for analyzing the modified lines coverage only.
     *
     * @param content
     *         The original HTML content
     * @param fileNode
     *         The {@link FileNode node} which represents the coverage of the file
     *
     * @return the filtered HTML sourcecode view
     */
    public String calculateModifiedLinesCoverageSourceCode(final String content, final FileNode fileNode) {
        Set<Integer> lines = fileNode.getLinesWithCoverage();
        lines.retainAll(fileNode.getModifiedLines());
        Set<String> linesAsText = lines.stream().map(String::valueOf).collect(Collectors.toSet());
        Document doc = Jsoup.parse(content, Parser.xmlParser());
        Elements elements = doc.select("tr");
        for (Element element : elements) {
            String line = element.select("td > a").text();
            if (!linesAsText.contains(line)) {
                if (element.hasAttr(TOOLTIP_ATTR)) {
                    element.removeAttr(TOOLTIP_ATTR);
                }
                element.removeClass(element.className());
                element.addClass("noCover");
                Objects.requireNonNull(element.select("td.hits").first()).text("");
            }
        }
        return doc.html();
    }
}
