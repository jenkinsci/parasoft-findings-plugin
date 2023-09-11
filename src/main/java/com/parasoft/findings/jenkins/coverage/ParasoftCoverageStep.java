package com.parasoft.findings.jenkins.coverage;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;

import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageRecorder;
import io.jenkins.plugins.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder.*;
public class ParasoftCoverageStep extends Step implements Serializable {
    private static final long serialVersionUID = -2235239576082380147L;
    private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();
    private String pattern;
    private String sourceCodeEncoding = StringUtils.EMPTY;
    private List<ParasoftCoverageQualityGate> parasoftCoverageQualityGates = new ArrayList<>();

    @DataBoundConstructor
    public ParasoftCoverageStep(){
        super();

        // empty constructor required for Stapler
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this);
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

    @SuppressWarnings("unused") // used by Stapler view data binding
    @DataBoundSetter
    public void setParasoftCoverageQualityGates(final List<ParasoftCoverageQualityGate> parasoftCoverageQualityGates) {
        if (parasoftCoverageQualityGates != null && !parasoftCoverageQualityGates.isEmpty()) {
            this.parasoftCoverageQualityGates = List.copyOf(parasoftCoverageQualityGates);
        } else {
            this.parasoftCoverageQualityGates = new ArrayList<>();
        }
    }

    public List<ParasoftCoverageQualityGate> getParasoftCoverageQualityGates() {
        return parasoftCoverageQualityGates;
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
            LogHandler logHandler = new LogHandler(taskListener, PARASOFT_COVERAGE_NAME);
            CoverageConversionResult coverageResult = parasoftCoverageRecorder.performCoverageReportConversion(
                    run, workspace, logHandler, runResultHandler);
            CoverageRecorder recorder = setUpCoverageRecorder(coverageResult.getCoberturaPattern(), step.getSourceCodeEncoding(),
                    step.getParasoftCoverageQualityGates());

            recorder.perform(run, workspace, taskListener, runResultHandler);
            parasoftCoverageRecorder.deleteTemporaryCoverageDirs(workspace, coverageResult.getGeneratedCoverageBuildDirs(), logHandler);
            return UNUSED;
        }
    }

    @Extension
    public static class ParasoftCoverageStepDescriptor extends StepDescriptor {
        private static final JenkinsFacade JENKINS = new JenkinsFacade();

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

}
