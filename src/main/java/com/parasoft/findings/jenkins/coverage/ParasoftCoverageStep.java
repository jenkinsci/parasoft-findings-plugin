package com.parasoft.findings.jenkins.coverage;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;

import hudson.util.ReflectionUtils;
import io.jenkins.plugins.coverage.metrics.steps.CoverageRecorder;
import io.jenkins.plugins.util.AbstractExecution;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.RunResultHandler;
import io.jenkins.plugins.util.StageResultHandler;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import static com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder.*;
public class ParasoftCoverageStep extends Step implements Serializable {
    private static final long serialVersionUID = -2235239576082380147L;
    private String pattern;
    private String sourceCodeEncoding;

    @DataBoundConstructor
    public ParasoftCoverageStep(String pattern, String sourceCodeEncoding){
        this.pattern = pattern;
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this);
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    @CheckForNull
    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    @SuppressFBWarnings(value = "THROWS", justification = "false positive")
    static class Execution extends AbstractExecution<Void> {
        private static final long serialVersionUID = -6177818067217577567L;
        private static final Void UNUSED = null;
        private final ParasoftCoverageStep step;


        Execution(@NonNull final StepContext context, final ParasoftCoverageStep step) throws Exception {
            super(context);
            this.step = step;
        }

        @Override
        @CheckForNull
        protected Void run() throws Exception {
            Run<?, ?> run = getRun();
            FilePath workspace = getWorkspace();
            TaskListener taskListener = getTaskListener();
            RunResultHandler runResultHandler = new RunResultHandler(run);
            var parasoftCoverageRecorder = new ParasoftCoverageRecorder();
            parasoftCoverageRecorder.setPattern(step.getPattern());
            parasoftCoverageRecorder.setSourceCodeEncoding(step.getSourceCodeEncoding());
            LogHandler logHandler = new LogHandler(taskListener, PARASOFT_COVERAGE_NAME);
            ParasoftCoverageRecorder.CoverageConversionResult coverageResult = parasoftCoverageRecorder.performCoverageReportConversion(
                    run, workspace, logHandler, runResultHandler);
            CoverageRecorder recorder = setUpCoverageRecorder(coverageResult.getCoberturaPattern(), coverageResult.getSourceCodeEncoding());

            // Using reflection for calling the 'perform' method of the 'CoverageRecorder' class.
            // Argument 'AbstractBuild<?, ?>' cannot be directly passed in.
            Method performMethod =
                    ReflectionUtils.findMethod(CoverageRecorder.class, "perform", Run.class, FilePath.class,
                            TaskListener.class, StageResultHandler.class); // $NON-NLS-1$
            ReflectionUtils.makeAccessible(Objects.requireNonNull(performMethod));
            ReflectionUtils.invokeMethod(performMethod, recorder, run, workspace, taskListener,
                    runResultHandler);
            parasoftCoverageRecorder.deleteTemporaryCoverageDirs(workspace, coverageResult.getGeneratedCoverageBuildDirs(), logHandler);
            return UNUSED;
        }
    }

    @Extension
    public static class ParasoftCoverageStepDescriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(FilePath.class, FlowNode.class, Run.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "recordParasoftCoverage"; // $NON-NLS-1$
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Recorder_Name();
        }

        // Used in jelly file.
        public String defaultPattern() {
            return DEFAULT_PATTERN;
        }

        // Used in jelly file.
        public String defaultSourceCodeEncoding() {
            return DEFAULT_SOURCE_CODE_ENCODING;
        }
    }

}
