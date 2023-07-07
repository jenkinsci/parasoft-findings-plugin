package com.parasoft.findings.jenkins.coverage;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import io.jenkins.plugins.coverage.metrics.steps.CoverageStep;
import io.jenkins.plugins.coverage.metrics.steps.CoverageTool;
import io.jenkins.plugins.util.AbstractExecution;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.RunResultHandler;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ParasoftCoverageStep extends Step implements Serializable {
    private static final long serialVersionUID = -2235239576082380147L;
    private String pattern = StringUtils.EMPTY;
    private static final String DEFAULT_PATTERN = "**/coverage.xml";
    private static final String PARASOFT_COVERAGE_ID = "parasoft-coverage";
    private static final String PARASOFT_COVERAGE_NAME = "parasoft-coverage";

    @DataBoundConstructor
    public ParasoftCoverageStep(String pattern){
        this.pattern = pattern;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this);
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    @SuppressFBWarnings(value = "THROWS", justification = "false positive")
    static class Execution extends AbstractExecution<Void> {
        private static final long serialVersionUID = -6177818067217577567L;
        private static final Void UNUSED = null;
        private final ParasoftCoverageStep step;
        private final StepContext stepContext;

        Execution(@NonNull final StepContext context, final ParasoftCoverageStep step) throws Exception {
            super(context);
            this.step = step;
            this.stepContext = context;
        }

        @Override
        @CheckForNull
        protected Void run() throws Exception {
            CoverageTool tool = new CoverageTool();
            var parasoftCoverageRecorder = new ParasoftCoverageRecorder();
            parasoftCoverageRecorder.setPattern(step.getPattern());
            LogHandler logHandler = new LogHandler(getTaskListener(), PARASOFT_COVERAGE_NAME);
            ParasoftCoverageRecorder.CoverageConversionResult coverageResult = parasoftCoverageRecorder.performCoverageReportConversion(
                    getRun(), getWorkspace(), logHandler, new RunResultHandler(getRun()));
            tool.setParser(CoverageTool.Parser.COBERTURA);
            tool.setPattern(coverageResult.getCoberturaPattern());
            CoverageStep coverageStep = new CoverageStep();
            coverageStep.start(stepContext).getContext().get(TaskListener.class);
            coverageStep.setTools(List.of(tool));
            coverageStep.setId(PARASOFT_COVERAGE_ID);
            coverageStep.setName(PARASOFT_COVERAGE_NAME);
            StepExecution execution = coverageStep.start(stepContext);
            execution.start();
            return UNUSED;
        }
    }

    @Extension
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(FilePath.class, FlowNode.class, Run.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "parasoftCoverage";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Record Parasoft code coverage results";
        }

        // Used in jelly file.
        public String defaultPattern() {
            return DEFAULT_PATTERN;
        }
    }

}
