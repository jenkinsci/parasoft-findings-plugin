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

import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.util.VisibleForTesting;

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

/**
 * Defines a quality gate based on a specific threshold of code coverage in the current build. After a build has been
 * finished, a set of {@link CoverageQualityGate quality gates} will be evaluated and the overall quality gate status will be
 * reported in Jenkins UI.
 *
 * @author Johannes Walter
 */
public class CoverageQualityGate extends QualityGate {
    private static final long serialVersionUID = -397278599489426668L;

    private static final ElementFormatter FORMATTER = new ElementFormatter();

    private Baseline type = Baseline.PROJECT;

    /**
     * Creates a new instance of {@link CoverageQualityGate}.
     *
     * @param threshold
     *         minimum or maximum value that triggers this quality gate
     */
    @DataBoundConstructor
    public CoverageQualityGate(final double threshold, final Baseline type, final QualityGateCriticality criticality) {
        super(checkThresholdRange(threshold));

        setType(type);
        setCriticality(criticality);
    }

    /**
     * Sets the baseline that will be used for the quality gate evaluation.
     *
     * @param type
     *         the baseline to use
     */
    @DataBoundSetter
    public final void setType(final Baseline type) {
        this.type = type;
    }

    /**
     * Returns a human-readable name of the quality gate.
     *
     * @return a human-readable name
     */
    @Override
    public String getName() {
        return String.format("%s - %s", FORMATTER.getDisplayName(getType()),
                FORMATTER.getDisplayName(getMetric()));
    }

    public Metric getMetric() {
        return Metric.LINE;
    }

    public Baseline getType() {
        return type;
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
        public ListBoxModel doFillTypeItems(@AncestorInPath final AbstractProject<?, ?> project) {
            if (jenkins.hasPermission(Item.CONFIGURE, project)) {
                return FORMATTER.getTypeItems();
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
    }

    private static double checkThresholdRange(double threshold) {
        return threshold < 0 ? 0 : threshold > 100 ? 100 : threshold;
    }
}
