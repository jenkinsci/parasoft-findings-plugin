package com.parasoft.findings.jenkins.coverage;

import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageQualityGate;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;

import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
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
    private String sourceCodeEncoding;
    private List<CoverageQualityGate> CoverageQualityGates = new ArrayList<>();
    private String referenceBuild;

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
    public void setReferenceBuild(String referenceBuild) {
        this.referenceBuild = referenceBuild;
    }

    public String getReferenceBuild() {
        return referenceBuild;
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
    public void setCoverageQualityGates(final List<CoverageQualityGate> CoverageQualityGates) {
        if (CoverageQualityGates != null && !CoverageQualityGates.isEmpty()) {
            this.CoverageQualityGates = List.copyOf(CoverageQualityGates);
        } else {
            this.CoverageQualityGates = new ArrayList<>();
        }
    }

    public List<CoverageQualityGate> getCoverageQualityGates() {
        return CoverageQualityGates;
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
            ParasoftCoverageRecorder recorder = setUpCoverageRecorder(step.getPattern(), step.getSourceCodeEncoding(),
                    step.getCoverageQualityGates(), step.getReferenceBuild());

            recorder.perform(run, workspace, taskListener, runResultHandler);
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

    static ParasoftCoverageRecorder setUpCoverageRecorder(final String pattern, final String sourceCodeEncoding,
                                                          final List<CoverageQualityGate> coverageQualityGates, final String referenceBuild) {
        ParasoftCoverageRecorder recorder = new ParasoftCoverageRecorder();
        recorder.setPattern(pattern);
        recorder.setCoverageQualityGates(coverageQualityGates);
        if (referenceBuild != null && !referenceBuild.isEmpty()) {
            recorder.setReferenceBuild(referenceBuild);
        }
        if (sourceCodeEncoding != null && !sourceCodeEncoding.isEmpty()) {
            recorder.setSourceCodeEncoding(sourceCodeEncoding);
        }
        return recorder;
    }
}
