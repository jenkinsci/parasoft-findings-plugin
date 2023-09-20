package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.util.List;
import java.util.Optional;

import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Value;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.Functions;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider.DisplayColors;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.ElementFormatter;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Dashboard column model which represents coverage metrics of different coverage types.
 *
 * @author Florian Orendi
 */
public class CoverageMetricColumn extends ListViewColumn {
    private static final ElementFormatter FORMATTER = new ElementFormatter();
    private final Metric metric = Metric.LINE;
    private final Baseline baseline = Baseline.PROJECT;

    /**
     * Creates a new column.
     */
    @DataBoundConstructor
    public CoverageMetricColumn() {
        super();
    }

    public ElementFormatter getFormatter() {
        return FORMATTER;
    }

    public String getColumnName() {
        return Messages.Coverage_Column();
    }


    /**
     * Returns all available values for the specified baseline.
     *
     * @param job
     *         the job in the current row
     *
     * @return the available values
     */
    // Called by jelly view
    public List<Value> getAllValues(final Job<?, ?> job) {
        return findAction(job).map(a -> a.getAllValues(baseline)).orElse(List.of());
    }

    /**
     * Returns a formatted and localized String representation of the specified value (without metric).
     *
     * @param value
     *         the value to format
     *
     * @return the value formatted as a string
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String formatMetric(final Value value) {
        return FORMATTER.getDisplayName(value.getMetric());
    }

    /**
     * Returns a formatted and localized String representation of the specified value (without metric).
     *
     * @param value
     *         the value to format
     *
     * @return the value formatted as a string
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String formatValue(final Value value) {
        return FORMATTER.formatDetails(value, Functions.getCurrentLocale());
    }

    /**
     * Provides a text which represents the coverage percentage of the selected coverage type and metric.
     *
     * @param job
     *         the job in the current row
     *
     * @return the coverage text
     */
    public String getCoverageText(final Job<?, ?> job) {
        Optional<? extends Value> coverageValue = getCoverageValue(job);
        if (coverageValue.isPresent()) {
            return FORMATTER.format(coverageValue.get(), Functions.getCurrentLocale());
        }
        return Messages.Coverage_Not_Available();
    }

    /**
     * Provides the coverage value of the selected coverage type and metric.
     *
     * @param job
     *         the job in the current row
     *
     * @return the coverage percentage
     */
    public Optional<? extends Value> getCoverageValue(final Job<?, ?> job) {
        return findAction(job).flatMap(action -> action.getStatistics().getValue(baseline, metric));
    }

    private static Optional<CoverageBuildAction> findAction(final Job<?, ?> job) {
        var lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(lastCompletedBuild.getAction(CoverageBuildAction.class));
    }

    /**
     * Provides the line color for representing the passed coverage value.
     *
     * @param job
     *         the job in the current row
     * @param coverage
     *         The coverage value as percentage
     *
     * @return the line color as hex string
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DisplayColors getDisplayColors(final Job<?, ?> job, final Optional<? extends Value> coverage) {
        if (coverage.isPresent() && hasCoverageAction(job)) {
            return FORMATTER.getDisplayColors(baseline, coverage.get());
        }
        return ColorProvider.DEFAULT_COLOR;
    }

    /**
     * Provides the relative URL which can be used for accessing the coverage report.
     *
     * @param job
     *         the job in the current row
     *
     * @return the relative URL or an empty string when there is no matching URL
     */
    public String getRelativeCoverageUrl(final Job<?, ?> job) {
        if (hasCoverageAction(job)) {
            CoverageBuildAction action = job.getLastCompletedBuild().getAction(CoverageBuildAction.class);
            return action.getUrlName() + "/" + baseline.getUrl();
        }
        return "";
    }

    /**
     * Transforms percentages with a ',' decimal separator to a representation using a '.' in order to use the
     * percentage for styling HTML tags.
     *
     * @param percentage
     *         The text representation of a percentage
     *
     * @return the formatted percentage string
     */
    public String getBackgroundColorFillPercentage(final String percentage) {
        return FORMATTER.getBackgroundColorFillPercentage(percentage);
    }

    /**
     * Checks whether a {@link CoverageBuildAction} exists within the completed build.
     *
     * @param job
     *         the job in the current row
     *
     * @return {@code true} whether the action exists, else {@code false}
     */
    private boolean hasCoverageAction(final Job<?, ?> job) {
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        return lastCompletedBuild != null && !lastCompletedBuild.getActions(CoverageBuildAction.class).isEmpty();
    }

    /**
     * Descriptor of the column.
     */
    @Extension(optional = true)
    @Symbol("coverageTotalsColumn")
    public static class CoverageMetricColumnDescriptor extends ListViewColumnDescriptor {
        /**
         * Creates a new descriptor.
         */
        @SuppressWarnings("unused") // Required for Jenkins Extensions
        public CoverageMetricColumnDescriptor() {
            this(new JenkinsFacade());
        }

        @VisibleForTesting
        CoverageMetricColumnDescriptor(final JenkinsFacade jenkins) {
            super();

            this.jenkins = jenkins;
        }

        private final JenkinsFacade jenkins;

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Coverage_Column();
        }
    }
}
