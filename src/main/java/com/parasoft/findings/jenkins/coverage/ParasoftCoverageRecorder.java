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
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import io.jenkins.plugins.coverage.metrics.steps.CoverageRecorder;
import io.jenkins.plugins.coverage.metrics.steps.CoverageTool;
import io.jenkins.plugins.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ParasoftCoverageRecorder extends Recorder {

    private static final String PARASOFT_COVERAGE_ID = "parasoft-coverage";
    private static final String PARASOFT_COVERAGE_NAME = "Parasoft Coverage";
    private static final String DEFAULT_PATTERN = "**/coverage.xml";
    private static final String COBERTURA_XSL_NAME = "cobertura.xsl";
    private static final String FILE_PATTERN_SEPARATOR = ",";

    private String pattern = StringUtils.EMPTY;

    @DataBoundConstructor
    public ParasoftCoverageRecorder() {
        super();
        // empty constructor required for Stapler
    }

    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        FilePath workspace = build.getWorkspace();
        StageResultHandler resultHandler = new RunResultHandler(build);
        LogHandler logHandler = new LogHandler(listener, PARASOFT_COVERAGE_NAME);
        FilteredLog log = new FilteredLog("Errors while recording Parasoft code coverage:");
        log.logInfo("Recording Parasoft coverage results");
        String coberturaPattern = convertParasoftCoverageToCoberturaCoverage(build, workspace, resultHandler, log,
                logHandler);
        CoverageRecorder recorder = setUpCoverageRecorder(coberturaPattern);
        return recorder.perform(build, launcher, listener);
    }

    @Override
    public ParasoftCoverageDescriptor getDescriptor() {
        return (ParasoftCoverageDescriptor) super.getDescriptor();
    }

    private static CoverageRecorder setUpCoverageRecorder(final String pattern) {
        CoverageRecorder recorder = new CoverageRecorder();
        CoverageTool tool = new CoverageTool();
        tool.setParser(CoverageTool.Parser.COBERTURA);
        tool.setPattern(pattern);
        recorder.setTools(List.of(tool));
        recorder.setId(PARASOFT_COVERAGE_ID);
        recorder.setName(PARASOFT_COVERAGE_NAME);
        return recorder;
    }

    private String convertParasoftCoverageToCoberturaCoverage(final Run<?, ?> run, final FilePath workspace,
                                                              final StageResultHandler resultHandler,
                                                              final FilteredLog log,
                                                              final LogHandler logHandler) throws InterruptedException {
        List<String> results = new ArrayList<>();

        String expandedPattern = expandPattern(run, pattern);
        if (!expandedPattern.equals(pattern)) {
            log.logInfo("Expanding pattern '%s' to '%s'", pattern, expandedPattern);
        }

        if (StringUtils.isBlank(expandedPattern)) {
            log.logInfo("Using default pattern '%s' since user defined pattern is not set", DEFAULT_PATTERN);
            expandedPattern = DEFAULT_PATTERN;
        }

        boolean failTheBuild = false;
        try {
            AgentFileVisitor.FileVisitorResult<String> result = workspace.act(
                    new ParasoftCoverageReportScanner(expandedPattern, getCoberturaXslContent(), workspace.getRemote(),
                            StandardCharsets.UTF_8.name(), false));
            log.merge(result.getLog());

            List<String> coverageResults = result.getResults();
            if (result.hasErrors()) {
                failTheBuild = true;
            }
            results.addAll(coverageResults);
        } catch (IOException exception) {
            log.logException(exception, "Exception while converting Parasoft coverage to Cobertura coverage");
            failTheBuild = true;
        }

        if (failTheBuild) {
            String errorMessage = "Failing build due to some errors during recording of the Parasoft coverage";
            log.logInfo(errorMessage);
            resultHandler.setResult(Result.FAILURE, errorMessage);
        }

        logHandler.log(log);

        return String.join(FILE_PATTERN_SEPARATOR, results);
    }

    private String expandPattern(final Run<?, ?> run, final String actualPattern) {
        try {
            EnvironmentResolver environmentResolver = new EnvironmentResolver();

            return environmentResolver.expandEnvironmentVariables(
                    run.getEnvironment(TaskListener.NULL), actualPattern);
        }
        catch (IOException | InterruptedException ignore) {
            return actualPattern; // fallback, no expansion
        }
    }

    private String getCoberturaXslContent() throws IOException {
        try (InputStream coberturaXslInput = this.getClass().getResourceAsStream(COBERTURA_XSL_NAME)) {
            if (coberturaXslInput == null) {
                throw new IOException("Failed to read Cobertura XSL.");
            }
            return IOUtils.toString(coberturaXslInput, StandardCharsets.UTF_8);
        }
    }

    @Extension
    public static class ParasoftCoverageDescriptor extends BuildStepDescriptor<Publisher> {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Recorder_Name();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        // Used in jelly file.
        public String defaultPattern() {
            return DEFAULT_PATTERN;
        }
    }
}
