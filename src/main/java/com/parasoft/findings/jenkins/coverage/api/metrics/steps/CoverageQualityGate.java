package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.util.VisibleForTesting;

import hudson.model.PermalinkProjectAction;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.util.ListBoxModel;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.ElementFormatter;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGate;

import static hudson.model.PermalinkProjectAction.Permalink.LAST_SUCCESSFUL_BUILD;

/**
 * Defines a quality gate based on a specific threshold of code coverage in the current build. After a build has been
 * finished, a set of {@link CoverageQualityGate quality gates} will be evaluated and the overall quality gate status will be
 * reported in Jenkins UI.
 *
 * @author Johannes Walter
 */
public class CoverageQualityGate extends QualityGate {
    private static final long serialVersionUID = -397278599489426668L;

    private static final PermalinkProjectAction.Permalink DEFAULT_REFERENCE_BUILD = LAST_SUCCESSFUL_BUILD;

    private static final ElementFormatter FORMATTER = new ElementFormatter();

    private Baseline baseline = Baseline.PROJECT;

    private String referenceBuild = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link CoverageQualityGate}.
     *
     * @param threshold
     *         minimum or maximum value that triggers this quality gate
     */
    @DataBoundConstructor
    public CoverageQualityGate(final double threshold, final Metric metric, final Baseline baseline, final QualityGateCriticality criticality) {
        super(threshold);

        setBaseline(baseline);
        setCriticality(criticality);
    }

    /**
     * Sets the baseline that will be used for the quality gate evaluation.
     *
     * @param baseline
     *         the baseline to use
     */
    @DataBoundSetter
    public final void setBaseline(final Baseline baseline) {
        this.baseline = baseline;
    }

    @DataBoundSetter
    public void setReferenceBuild(String referenceBuild) {
        this.referenceBuild = referenceBuild;
    }

    public String getReferenceBuild() {
        return referenceBuild;
    }

    public String getActualReferenceBuild() {
        return StringUtils.defaultIfBlank(referenceBuild, DEFAULT_REFERENCE_BUILD.getId());
    }

    /**
     * Returns a human-readable name of the quality gate.
     *
     * @return a human-readable name
     */
    @Override
    public String getName() {
        return String.format("%s - %s", FORMATTER.getDisplayName(getBaseline()),
                FORMATTER.getDisplayName(getMetric()));
    }

    public Metric getMetric() {
        return Metric.LINE;
    }

    public Baseline getBaseline() {
        return baseline;
    }

    /**
     * Descriptor of the {@link CoverageQualityGate}.
     */
    @Extension
    public static class Descriptor extends QualityGateDescriptor {
        private final JenkinsFacade jenkins;

        @VisibleForTesting
        Descriptor(final JenkinsFacade jenkinsFacade) {
            super();

            jenkins = jenkinsFacade;
        }

        /**
         * Creates a new descriptor.
         */
        @SuppressWarnings("unused") // Required for Jenkins Extensions
        public Descriptor() {
            this(new JenkinsFacade());
        }

        /**
         * Returns a model with all {@link Metric metrics} that can be used in quality gates.
         *
         * @param project
         *         the project that is configured
         *
         * @return a model with all {@link Metric metrics}.
         */
        @POST
        @SuppressWarnings("unused") // used by Stapler view data binding
        public ListBoxModel doFillBaselineItems(@AncestorInPath final AbstractProject<?, ?> project) {
            if (jenkins.hasPermission(Item.CONFIGURE, project)) {
                return FORMATTER.getBaselineItems();
            }
            return new ListBoxModel();
        }

        @POST
        @SuppressWarnings("unused") // used by Stapler view data binding
        public ListBoxModel doFillCriticalityItems(@AncestorInPath final AbstractProject<?, ?> project) {
            if (jenkins.hasPermission(Item.CONFIGURE, project)) {
                return FORMATTER.getCriticalityItems();
            }
            return new ListBoxModel();
        }

        // Used in jelly file.
        @SuppressWarnings("unused")
        public String defaultReferenceBuild() {
            // The Simplified Chinese display name is provided by the "Localization: Chinese (Simplified)" plugin.
            // See: https://github.com/jenkinsci/localization-zh-cn-plugin/blob/fd4622d1bded6979c02ed611524fda9b2c0415f3/core/src/main/resources/hudson/model/Messages_zh_CN.properties#L304C12-L304C12
            return DEFAULT_REFERENCE_BUILD.getDisplayName();
        }
    }
}
