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

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.util.RemoteResultWrapper;
import jenkins.MasterToSlaveFileCallable;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Resolves source code files on the agent using the stored paths of the coverage reports.
 */
public class PathResolver {
    /**
     * Resolves source code files on the agent using the stored paths of the coverage reports.
     *
     * @param paths
     *         the paths to map
     * @param workspace
     *         the workspace that contains the source code files
     * @param log
     *         the log to write to
     *
     * @return the resolved paths as mapping of relative to absolute paths
     */
    public Map<String, String> resolvePaths(final Set<String> paths,
            final FilePath workspace, final FilteredLog log) throws InterruptedException {
        try {
            var resolver = new AgentPathResolver(paths);
            var agentLog = workspace.act(resolver);
            log.merge(agentLog);
            return agentLog.getResult();
        }
        catch (IOException exception) {
            log.logError("Can't resolve source files on agent due to an exception: %s", ExceptionUtils.getRootCauseMessage(exception));
        }
        return Collections.emptyMap();
    }

    /**
     * Resolves source code files on the agent using the stored paths of the coverage reports.
     */
    static class AgentPathResolver extends MasterToSlaveFileCallable<RemoteResultWrapper<HashMap<String, String>>> {
        private static final long serialVersionUID = 3966282357309568323L;
        private static final PathUtil PATH_UTIL = new PathUtil();

        private final Set<String> paths;

        /**
         * Creates a new instance of {@link AgentPathResolver}.
         *
         * @param paths
         *         the paths to map
         */
        AgentPathResolver(final Set<String> paths) {
            super();
            this.paths = paths;
        }

        @Override
        public RemoteResultWrapper<HashMap<String, String>> invoke(
                final File workspaceFile, final VirtualChannel channel) {
            FilteredLog log = new FilteredLog("Errors while resolving source files on agent:");

            log.logInfo("Searching for source code files...");

            var workspace = new FilePath(workspaceFile);
            var mapping = paths.stream()
                    .map(path -> new SimpleEntry<>(path, locateSource(path, workspace, log)))
                    .filter(entry -> entry.getValue().isPresent())
                    .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().get()));

            if (mapping.size() == paths.size()) {
                log.logInfo("-> resolved absolute paths for all %d source files", mapping.size());
            }
            else {
                log.logInfo("-> finished resolving of absolute paths (found: %d, not found: %d)",
                        mapping.size(), paths.size() - mapping.size());
            }

            var changedFilesMapping = mapping.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(entry.getValue()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            var result = new RemoteResultWrapper<>(new HashMap<>(changedFilesMapping), "Errors during source path resolving:");
            result.merge(log);
            return result;
        }

        private Optional<String> locateSource(final String relativePath, final FilePath workspace, final FilteredLog log) {

            try {
                FilePath absolutePath = new FilePath(new File(relativePath));
                if (absolutePath.exists()) {
                    return getPathFor(absolutePath, workspace);
                }

                FilePath relativePathInWorkspace = workspace.child(relativePath);
                if (relativePathInWorkspace.exists()) {
                    return getPathFor(relativePathInWorkspace, workspace);
                }

                log.logError("- Source file '%s' not found", relativePath);
            }
            catch (InvalidPathException | IOException | InterruptedException exception) {
                log.logError("No valid path in coverage node : '%s' due to an exception: %s", relativePath, ExceptionUtils.getRootCauseMessage(exception));
            }
            return Optional.empty();
        }

        private Optional<String> getPathFor(final FilePath absolutePath, final FilePath workspace) {
            var fileName = absolutePath.getRemote();
            if (isWithinWorkspace(fileName, workspace)) {
                return Optional.of(PATH_UTIL.getRelativePath(workspace.getRemote(), fileName));
            }
            else {
                return Optional.of(PATH_UTIL.getAbsolutePath(fileName));
            }
        }

        private boolean isWithinWorkspace(final String fileName, final FilePath workspace) {
            var workspacePath = PATH_UTIL.getAbsolutePath(workspace.getRemote());
            return PATH_UTIL.getAbsolutePath(fileName).startsWith(workspacePath);
        }
    }
}
