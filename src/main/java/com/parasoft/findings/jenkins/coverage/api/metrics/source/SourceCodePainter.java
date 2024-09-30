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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.parasoft.findings.jenkins.coverage.model.FileNode;
import com.parasoft.findings.jenkins.coverage.model.Node;
import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.FilePath;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.util.ValidationUtilities;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Highlights the code coverage information in all source code files. This process is executed on the agent node that
 * has all source files checked out.
 */
public class SourceCodePainter {
    private final Run<?, ?> build;
    private final FilePath workspace;
    private final String id;

    /**
     * Creates a painter for the passed build, using the passed properties.
     *
     * @param build
     *         The build which processes the source code
     * @param workspace
     *         The workspace which contains the source code files
     * @param id
     *         the ID of the coverage results - each ID will store the files in a separate directory
     */
    public SourceCodePainter(@NonNull final Run<?, ?> build, @NonNull final FilePath workspace, final String id) {
        this.build = build;
        this.workspace = workspace;
        this.id = id;
    }

    /**
     * Processes the source code painting.
     *
     * @param rootNode
     *         the root of the tree
     * @param files
     *         the files to paint
     * @param sourceCodeEncoding
     *         the encoding of the source code files
     * @param sourceCodeRetention
     *         the source code retention strategy
     * @param log
     *         The log
     *
     * @throws InterruptedException
     *         if the painting process has been interrupted
     */
    public void processSourceCodePainting(final Node rootNode, final List<FileNode> files,
            final String sourceCodeEncoding, final SourceCodeRetention sourceCodeRetention, final FilteredLog log)
            throws InterruptedException {
        SourceCodeFacade sourceCodeFacade = new SourceCodeFacade();
        if (sourceCodeRetention != SourceCodeRetention.NEVER) {
            var paintedFiles = files.stream()
                    .map(this::createFileModel)
                    .collect(Collectors.toList());
            log.logInfo("Painting %d source files on agent", paintedFiles.size());

            paintFilesOnAgent(paintedFiles, sourceCodeEncoding, log);
            log.logInfo("Copying painted sources from agent to build folder");

            sourceCodeFacade.copySourcesToBuildFolder(build, workspace, log);
        }
        sourceCodeRetention.cleanup(build, sourceCodeFacade.getCoverageSourcesDirectory(), log);
    }

    private CoverageSourcePrinter createFileModel(final FileNode fileNode) {
        return new CoverageSourcePrinter(fileNode);
    }

    private void paintFilesOnAgent(final List<? extends CoverageSourcePrinter> paintedFiles,
            final String sourceCodeEncoding, final FilteredLog log) throws InterruptedException {
        try {
            var painter = new AgentCoveragePainter(paintedFiles, sourceCodeEncoding, id);
            FilteredLog agentLog = workspace.act(painter);
            log.merge(agentLog);
        }
        catch (IOException exception) {
            log.logError("Can't paint and zip sources on the agent due to an exception: %s", ExceptionUtils.getRootCauseMessage(exception));
        }
    }

    /**
     * Paints source code files on the agent using the recorded coverage information. All files are stored as zipped
     * HTML files that contain the painted source code. In the last step all zipped source files are aggregated into a
     * single archive to simplify copying to the controller.
     */
    static class AgentCoveragePainter extends MasterToSlaveFileCallable<FilteredLog> { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
        private static final long serialVersionUID = 3966282357309568323L;

        private final List<? extends CoverageSourcePrinter> paintedFiles;
        private final String sourceCodeEncoding;
        private final String directory;

        /**
         * Creates a new instance of {@link AgentCoveragePainter}.
         *
         * @param files
         *         the pretty printers for the files to create the HTML reports for
         * @param sourceCodeEncoding
         *         the encoding of the source code files
         * @param directory
         *         the subdirectory where the source files will be stored in
         */
        AgentCoveragePainter(final List<? extends CoverageSourcePrinter> files, final String sourceCodeEncoding,
                final String directory) {
            super();

            this.paintedFiles = files;
            this.sourceCodeEncoding = sourceCodeEncoding;
            this.directory = directory;
        }

        @Override
        public FilteredLog invoke(final File workspaceFile, final VirtualChannel channel) {
            FilteredLog log = new FilteredLog("Errors during source code painting:");
            FilePath workspace = new FilePath(workspaceFile);

            try {
                FilePath outputFolder = workspace.child(directory);
                outputFolder.mkdirs();

                Path temporaryFolder = Files.createTempDirectory(directory);

                int count = paintedFiles.parallelStream()
                        .mapToInt(file -> paintSource(file, workspace, temporaryFolder, log))
                        .sum();

                if (count == paintedFiles.size()) {
                    log.logInfo("-> finished painting successfully");
                }
                else {
                    log.logInfo("-> finished painting (%d files have been painted, %d files failed)",
                            count, paintedFiles.size() - count);
                }

                FilePath zipFile = workspace.child(SourceCodeFacade.COVERAGE_SOURCES_ZIP);
                outputFolder.zip(zipFile);
                log.logInfo("-> zipping sources from folder '%s' as '%s'", outputFolder, zipFile);

                deleteFolder(temporaryFolder.toFile(), log);
            }
            catch (IOException exception) {
                log.logError("Cannot create temporary directory for the painted source files due to an exception: %s", ExceptionUtils.getRootCauseMessage(exception));
            }
            catch (InterruptedException exception) {
                log.logError("Processing has been interrupted: skipping zipping of source files in folder %s due to an exception: %s", workspace, ExceptionUtils.getRootCauseMessage(exception));
            }

            return log;
        }

        private Charset getCharset() {
            return new ValidationUtilities().getCharset(sourceCodeEncoding);
        }

        private int paintSource(final CoverageSourcePrinter fileNode, final FilePath workspace,
                final Path temporaryFolder, final FilteredLog log) {
            String relativePathIdentifier = fileNode.getPath();
            FilePath paintedFilesDirectory = workspace.child(directory);
            return findSourceFile(workspace, relativePathIdentifier, log)
                    .map(resolvedPath -> paint(fileNode, relativePathIdentifier, resolvedPath,
                            paintedFilesDirectory, temporaryFolder, getCharset(), log))
                    .orElse(0);
        }

        private int paint(final CoverageSourcePrinter paint, final String relativePathIdentifier,
                final FilePath resolvedPath, final FilePath paintedFilesDirectory,
                final Path temporaryFolder, final Charset charset, final FilteredLog log) {
            String sanitizedFileName = SourceCodeFacade.sanitizeFilename(relativePathIdentifier);
            FilePath zipOutputPath = paintedFilesDirectory.child(
                    sanitizedFileName + SourceCodeFacade.ZIP_FILE_EXTENSION);
            try {
                Path paintedFilesFolder = Files.createTempDirectory(temporaryFolder, directory);
                Path fullSourcePath = paintedFilesFolder.resolve(sanitizedFileName);
                try (BufferedWriter output = Files.newBufferedWriter(fullSourcePath)) {
                    List<String> lines = Files.readAllLines(Paths.get(resolvedPath.getRemote()), charset);
                    for (int line = 0; line < lines.size(); line++) {
                        output.write(paint.renderLine(line + 1, lines.get(line)));
                    }
                }
                new FilePath(fullSourcePath.toFile()).zip(zipOutputPath);
                FileUtils.deleteDirectory(paintedFilesFolder.toFile());
                return 1;
            }
            catch (IOException | InterruptedException exception) {
                log.logError("Can't write coverage paint of '%s' to zipped source file '%s' due to an exception: %s",
                        relativePathIdentifier, zipOutputPath, ExceptionUtils.getRootCauseMessage(exception));
                return 0;
            }
        }

        private Optional<FilePath> findSourceFile(final FilePath workspace, final String fileName,
                final FilteredLog log) {
            try {
                FilePath absolutePath = new FilePath(new File(fileName));
                if (absolutePath.exists()) {
                    return Optional.of(absolutePath);
                }

                FilePath relativePath = workspace.child(fileName);
                if (relativePath.exists()) {
                    return Optional.of(relativePath);
                }
            }
            catch (InvalidPathException | IOException | InterruptedException exception) {
                log.logError("No valid path in coverage node: '%s' due to an exception: %s", fileName, ExceptionUtils.getRootCauseMessage(exception));
            }
            return Optional.empty();
        }

        /**
         * Deletes a folder.
         *
         * @param folder
         *         The directory to be deleted
         * @param log
         *         The log
         */
        private void deleteFolder(final File folder, final FilteredLog log) {
            if (folder.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(folder);
                }
                catch (IOException e) {
                    log.logError("The folder '%s' could not be deleted",
                            folder.getAbsolutePath());
                }
            }
        }
    }

}
