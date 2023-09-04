package com.parasoft.findings.jenkins.coverage;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGate;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;

public class ParasoftCoverageQualityGate extends QualityGate {
    private static final long serialVersionUID = 7809201823889916005L;
    private static final ParasoftCoverageQualityGateElementFormatter FORMATTER = new ParasoftCoverageQualityGateElementFormatter();

    private Baseline baseline = Baseline.PROJECT;

    @DataBoundConstructor
    public ParasoftCoverageQualityGate(final double threshold,
                                       final Baseline baseline, final QualityGateCriticality criticality) {
        super(threshold);
        setBaseline(baseline);
        setCriticality(criticality);
    }

    @DataBoundSetter
    public final void setBaseline(final Baseline baseline) {
        this.baseline = baseline;
    }

    @Override
    public String getName() {
        return String.format("%s - %s", FORMATTER.getDisplayName(this.getBaseline()), FORMATTER.getMetricLineDisplayName());
    }

    public Baseline getBaseline() {
        return baseline;
    }

    @Extension
    public static class ParasoftQualityGateDescriptor extends QualityGateDescriptor {
        private final JenkinsFacade JENKINS;

        @VisibleForTesting
        ParasoftQualityGateDescriptor(final JenkinsFacade jenkinsFacade) {
            super();
            JENKINS = jenkinsFacade;
        }

        @SuppressWarnings("unused")
        public ParasoftQualityGateDescriptor() {
            this(new JenkinsFacade());
        }

        @POST
        @SuppressWarnings("unused") // used by Stapler view data binding
        public ListBoxModel doFillBaselineItems(@AncestorInPath final AbstractProject<?, ?> project) {
            if (JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FORMATTER.getBaselineItems();
            }
            return new ListBoxModel();
        }

        @POST
        @SuppressWarnings("unused") // used by Stapler view data binding
        public ListBoxModel doFillCriticalityItems(@AncestorInPath final AbstractProject<?, ?> project) {
            if (JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FORMATTER.getCriticalityItems();
            }
            return new ListBoxModel();
        }
    }
}
