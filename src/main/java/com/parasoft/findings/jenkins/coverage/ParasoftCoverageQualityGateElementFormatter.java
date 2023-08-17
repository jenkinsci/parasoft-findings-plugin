package com.parasoft.findings.jenkins.coverage;

import edu.hm.hafner.coverage.Metric;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.jenkins.plugins.util.QualityGate;

import java.util.NoSuchElementException;

public final class ParasoftCoverageQualityGateElementFormatter {
    public ListBoxModel getBaselineItems() {
        ListBoxModel options = new ListBoxModel();
        add(options, Baseline.PROJECT);
        add(options, Baseline.MODIFIED_LINES);
        add(options, Baseline.MODIFIED_FILES);
        add(options, Baseline.PROJECT_DELTA);
        add(options, Baseline.MODIFIED_LINES_DELTA);
        add(options, Baseline.MODIFIED_FILES_DELTA);
        return options;
    }

    public String getDisplayName(final Baseline baseline) {
        switch (baseline) {
            case PROJECT:
                return Messages.Baseline_PROJECT();
            case MODIFIED_LINES:
                return Messages.Baseline_MODIFIED_LINES();
            case MODIFIED_FILES:
                return Messages.Baseline_MODIFIED_FILES();
            case PROJECT_DELTA:
                return Messages.Baseline_PROJECT_DELTA();
            case MODIFIED_LINES_DELTA:
                return Messages.Baseline_MODIFIED_LINES_DELTA();
            case MODIFIED_FILES_DELTA:
                return Messages.Baseline_MODIFIED_FILES_DELTA();
            default:
                throw new NoSuchElementException("No display name found for baseline " + baseline);
        }
    }

    public ListBoxModel getMetricItems() {
        ListBoxModel options = new ListBoxModel();
        add(options, Metric.FILE);
        add(options, Metric.LINE);
        return options;
    }

    public String getDisplayName(final Metric metric) {
        switch (metric) {
            case FILE:
                return Messages.Metric_FILE();
            case LINE:
                return Messages.Metric_LINE();
            default:
                throw new NoSuchElementException("No display name found for metric " + metric);
        }
    }

    public ListBoxModel getCriticalityItems() {
        ListBoxModel options = new ListBoxModel();
        add(options, QualityGate.QualityGateCriticality.UNSTABLE);
        add(options, QualityGate.QualityGateCriticality.FAILURE);
        return options;
    }

    public String getDisplayName(QualityGate.QualityGateCriticality criticality) {
        switch (criticality) {
            case UNSTABLE:
                return Messages.Criticality_UNSTABLE();
            case FAILURE:
                return Messages.Criticality_FAILURE();
            default:
                throw new NoSuchElementException("No display name found for criticality " + criticality);
        }
    }

    private void add(final ListBoxModel options, final Baseline baseline) {
        options.add(getDisplayName(baseline), baseline.name());
    }

    private void add(final ListBoxModel options, final Metric metric) {
        options.add(getDisplayName(metric), metric.name());
    }

    private void add(final ListBoxModel options, final QualityGate.QualityGateCriticality criticality) {
        options.add(getDisplayName(criticality), criticality.name());
    }
}
