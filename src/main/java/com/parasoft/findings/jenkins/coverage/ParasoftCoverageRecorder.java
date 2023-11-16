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

import com.parasoft.findings.jenkins.coverage.api.metrics.steps.*;
import com.parasoft.findings.jenkins.coverage.model.ModuleNode;
import com.parasoft.findings.jenkins.coverage.model.Node;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.TreeStringBuilder;
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
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import io.jenkins.plugins.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ParasoftCoverageRecorder extends Recorder {

    public static final String PARASOFT_COVERAGE_ID = "parasoft-coverage"; // $NON-NLS-1$
    public static final String PARASOFT_COVERAGE_NAME = "Parasoft Coverage"; // $NON-NLS-1$
    static final String DEFAULT_PATTERN = "**/coverage.xml"; // $NON-NLS-1$
    private static final String COBERTURA_XSL_NAME = "cobertura.xsl"; // $NON-NLS-1$
    private static final String FILE_PATTERN_SEPARATOR = ","; // $NON-NLS-1$
    private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();

    private String pattern = StringUtils.EMPTY;
    private String sourceCodeEncoding = StringUtils.EMPTY;
    private List<CoverageQualityGate> coverageQualityGates = new ArrayList<>();
    private String referenceJob = StringUtils.EMPTY;
    private String referenceBuild = StringUtils.EMPTY;

    @DataBoundConstructor
    public ParasoftCoverageRecorder() {
        super();
        // empty constructor required for Stapler
    }

    public String getId() {
        return PARASOFT_COVERAGE_ID;
    }

    public String getName() {
        return PARASOFT_COVERAGE_NAME;
    }

    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    @DataBoundSetter
    public void setSourceCodeEncoding(final String sourceCodeEncoding) {
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    @SuppressWarnings("unused")
    @DataBoundSetter
    public void setCoverageQualityGates(final List<CoverageQualityGate> coverageQualityGates) {
        if (coverageQualityGates != null && !coverageQualityGates.isEmpty()) {
            this.coverageQualityGates = List.copyOf(coverageQualityGates);
        } else {
            this.coverageQualityGates = new ArrayList<>();
        }
    }

    @SuppressWarnings("unused")
    public List<CoverageQualityGate> getCoverageQualityGates() {
        return coverageQualityGates;
    }

    @DataBoundSetter
    public void setReferenceJob(String referenceJob) {
        this.referenceJob = referenceJob;
    }

    public String getReferenceJob() {
        return referenceJob;
    }

    @DataBoundSetter
    public void setReferenceBuild(String referenceBuild) {
        this.referenceBuild = referenceBuild;
    }

    public String getReferenceBuild() {
        return referenceBuild;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        FilePath workspace = build.getWorkspace();
        if (workspace == null) {
            throw new IOException("No workspace found for " + build); // $NON-NLS-1$
        }

        perform(build, workspace, listener, new RunResultHandler(build));

        return true;
    }

    public void perform(final Run<?, ?> run, final FilePath workspace, final TaskListener taskListener,
                        final StageResultHandler resultHandler) throws InterruptedException {
        Result overallResult = run.getResult();
        LogHandler logHandler = new LogHandler(taskListener, PARASOFT_COVERAGE_NAME);
        if (overallResult == null || overallResult.isBetterOrEqualTo(Result.UNSTABLE)) {
            FilteredLog log = new FilteredLog("Errors while recording Parasoft code coverage:");
            log.logInfo("Recording coverage results");

            perform(run, workspace, taskListener, resultHandler, log, logHandler);
        }
        else {
            logHandler.log("Skipping execution of coverage recorder since overall result is '%s'", overallResult);
        }
    }

    private void perform(final Run<?, ?> run, final FilePath workspace, final TaskListener taskListener,
                         final StageResultHandler resultHandler, final FilteredLog log, final LogHandler logHandler) throws InterruptedException {
        List<com.parasoft.findings.jenkins.coverage.model.Node> results = recordCoverageResults(run, workspace, taskListener, logHandler, log);

        if (!results.isEmpty()) {
            CoverageReporter reporter = new CoverageReporter();
            var rootNode = Node.merge(results);

            var sources = rootNode.getSourceFolders();
            resolveAbsolutePaths(rootNode, workspace, sources, log);
            logHandler.log(log);

            reporter.publishAction(getId(), getIcon(), rootNode, run, workspace, taskListener, getReferenceJob(),
                    getReferenceBuild(), getCoverageQualityGates(), getSourceCodeEncoding(), resultHandler);
        }
    }

    private List<Node> recordCoverageResults(final Run<?, ?> run, final FilePath workspace, final TaskListener taskListener,
                                             final LogHandler logHandler, final FilteredLog log) throws InterruptedException {
        List<Node> results = new ArrayList<>();
        CoverageConversionResult coverageConversionResult = performCoverageReportConversion(run, workspace, logHandler,
                new RunResultHandler(run), log);

        try {
            AgentFileVisitor.FileVisitorResult<ModuleNode> result = workspace.act(
                    new CoverageReportScanner(coverageConversionResult.getCoberturaPattern(), "UTF-8", false, CoverageTool.Parser.COBERTURA));
            log.merge(result.getLog());

            var coverageResults = result.getResults();
            results.addAll(coverageResults);
        }
        catch (IOException exception) {
            log.logException(exception, "Exception while parsing with " + CoverageTool.Parser.COBERTURA);
        }

        logHandler.log(log);
        deleteTemporaryCoverageDirs(workspace, coverageConversionResult.getGeneratedCoverageBuildDirs(), logHandler, log);

        return results;
    }

    private void resolveAbsolutePaths(final Node rootNode, final FilePath workspace, final Set<String> sources,
                                      final FilteredLog log) throws InterruptedException {
        log.logInfo("Resolving source code files...");
        var pathMapping = new PathResolver().resolvePaths(rootNode.getFiles(), sources, workspace, log);

        if (!pathMapping.isEmpty()) {
            log.logInfo("Making paths of " + pathMapping.size() + " source code files relative to workspace root...");
            var builder = new TreeStringBuilder();
            rootNode.getAllFileNodes().stream()
                    .filter(file -> pathMapping.containsKey(file.getRelativePath()))
                    .forEach(file -> file.setRelativePath(builder.intern(pathMapping.get(file.getRelativePath()))));
            builder.dedup();
        }
    }

    private String getIcon() {
        return CoverageTool.Parser.COBERTURA.getIcon();
    }

    // Return Cobertura patterns and temporary coverage directories for this build.
    CoverageConversionResult performCoverageReportConversion(final Run<?, ?> run, final FilePath workspace,
                                                             final LogHandler logHandler,
                                                             final StageResultHandler resultHandler,
                                                             final FilteredLog log)
            throws InterruptedException {
        log.logInfo("Recording Parasoft coverage results"); // $NON-NLS-1$
        return convertCoverageReport(run, workspace, resultHandler,
                log, logHandler);
    }

    @Override
    public ParasoftCoverageDescriptor getDescriptor() {
        return (ParasoftCoverageDescriptor) super.getDescriptor();
    }

    private CoverageConversionResult convertCoverageReport(final Run<?, ?> run, final FilePath workspace,
                                                           final StageResultHandler resultHandler,
                                                           final FilteredLog log,
                                                           final LogHandler logHandler) throws InterruptedException {
        String expandedPattern = formatExpandedPattern(expandPattern(run, pattern));
        if (StringUtils.isBlank(expandedPattern)) {
            log.logInfo("Using default pattern '%s' for '%s' since specified pattern is empty", DEFAULT_PATTERN, pattern); // $NON-NLS-1$
            expandedPattern = DEFAULT_PATTERN;
        } else if (!expandedPattern.equals(pattern)) {
            log.logInfo("Expanded pattern '%s' to '%s'", pattern, expandedPattern); // $NON-NLS-1$
        }

        Set<String> coberturaPatterns = new HashSet<>();
        Set<String> generatedCoverageBuildDirs = new HashSet<>();

        boolean failTheBuild = false;
        try {
            AgentFileVisitor.FileVisitorResult<ProcessedFileResult> result = workspace.act(
                    new ParasoftCoverageReportScanner(expandedPattern, getCoberturaXslContent(), workspace.getRemote(),
                            StandardCharsets.UTF_8.name(), false));
            log.merge(result.getLog());

            List<ProcessedFileResult> coverageResults = result.getResults();
            if (result.hasErrors()) {
                failTheBuild = true;
            }
            coberturaPatterns.addAll(coverageResults.stream()
                    .map(ProcessedFileResult::getCoberturaPattern)
                    .collect(Collectors.toSet()));
            generatedCoverageBuildDirs.addAll(coverageResults.stream()
                    .map(ProcessedFileResult::getGeneratedCoverageBuildDir)
                    .collect(Collectors.toSet()));
        } catch (IOException exception) {
            log.logException(exception, "Exception while converting Parasoft coverage to Cobertura coverage"); // $NON-NLS-1$
            failTheBuild = true;
        }

        if (failTheBuild) {
            String errorMessage = "Failing build due to some errors during recording of the Parasoft coverage"; // $NON-NLS-1$
            log.logInfo(errorMessage);
            resultHandler.setResult(Result.FAILURE, errorMessage);
        }

        logHandler.log(log);

        return new CoverageConversionResult(StringUtils.join(coberturaPatterns, FILE_PATTERN_SEPARATOR),
                generatedCoverageBuildDirs);
    }

    // Resolves build parameters in the pattern.
    private String expandPattern(final Run<?, ?> run, final String pattern) {
        try {
            EnvironmentResolver environmentResolver = new EnvironmentResolver();

            return environmentResolver.expandEnvironmentVariables(
                    run.getEnvironment(TaskListener.NULL), pattern);
        }
        catch (IOException | InterruptedException ignore) {
            return pattern; // fallback, no expansion
        }
    }

    private String getCoberturaXslContent() throws IOException {
        try (InputStream coberturaXslInput = this.getClass().getResourceAsStream(COBERTURA_XSL_NAME)) {
            if (coberturaXslInput == null) {
                throw new IOException("Failed to read Cobertura XSL."); // $NON-NLS-1$
            }
            return IOUtils.toString(coberturaXslInput, StandardCharsets.UTF_8);
        }
    }

    void deleteTemporaryCoverageDirs(final FilePath workspace, final Set<String> tempCoverageDirs,
                                             final LogHandler logHandler, final FilteredLog log)
            throws InterruptedException {
        logHandler.log("Deleting temporary coverage files"); // $NON-NLS-1$
        for (String tempCoverageDir : tempCoverageDirs) {
            try {
                FilePath tempCoverageDirParent = workspace.child(tempCoverageDir).getParent();
                if (tempCoverageDirParent != null) {
                    tempCoverageDirParent.deleteRecursive();
                }
            } catch (IOException exception) {
                log.logException(exception, "Failed to delete directory '%s' due to an exception: ", tempCoverageDir); // $NON-NLS-1$
            }
        }

        logHandler.log(log);
    }

    private String formatExpandedPattern(String expandedPattern) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();
        fileSet.setIncludes(expandedPattern);
        String[] matchedFiles = fileSet.mergeIncludes(antProject);
        if (matchedFiles == null || matchedFiles.length == 0) {
            return "";
        }
        List<String> nonEmptyPatterns = Arrays.stream(matchedFiles)
                .filter(pattern -> !pattern.isEmpty())
                .collect(Collectors.toList());
        return String.join(", ", nonEmptyPatterns);
    }

    @Extension
    @Symbol("recordParasoftCoverage") // $NON-NLS-1$
    public static class ParasoftCoverageDescriptor extends BuildStepDescriptor<Publisher> {
        private static final JenkinsFacade JENKINS = new JenkinsFacade();

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

        @POST
        @SuppressWarnings("unused") // used by Stapler view data binding
        public ComboBoxModel doFillSourceCodeEncodingItems(@AncestorInPath final AbstractProject<?, ?> project) {
            if (JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return VALIDATION_UTILITIES.getAllCharsets();
            }
            return new ComboBoxModel();
        }

        @POST
        @SuppressWarnings("unused") // used by Stapler view data binding
        public FormValidation doCheckSourceCodeEncoding(@AncestorInPath final AbstractProject<?, ?> project,
                                                        @QueryParameter final String sourceCodeEncoding) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            return VALIDATION_UTILITIES.validateCharset(sourceCodeEncoding);
        }
    }

    static class CoverageConversionResult {
        private final String coberturaPattern;
        private final Set<String> generatedCoverageBuildDirs;

        public CoverageConversionResult(String coberturaPattern, Set<String> generatedCoverageBuildDirs) {
            this.coberturaPattern = coberturaPattern;
            this.generatedCoverageBuildDirs = generatedCoverageBuildDirs;
        }

        public String getCoberturaPattern() {
            return coberturaPattern;
        }

        public Set<String> getGeneratedCoverageBuildDirs() {
            return generatedCoverageBuildDirs;
        }
    }
}
