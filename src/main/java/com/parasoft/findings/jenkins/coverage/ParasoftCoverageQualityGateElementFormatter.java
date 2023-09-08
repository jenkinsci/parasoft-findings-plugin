package com.parasoft.findings.jenkins.coverage;

import hudson.util.ListBoxModel;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import io.jenkins.plugins.util.QualityGate;

import java.util.NoSuchElementException;

public final class ParasoftCoverageQualityGateElementFormatter {
    public ListBoxModel getBaselineItems() {
        ListBoxModel options = new ListBoxModel();
        add(options, Baseline.PROJECT);
        add(options, Baseline.MODIFIED_LINES);
        add(options, Baseline.PROJECT_DELTA);
        return options;
    }

    public String getDisplayName(final Baseline baseline) {
        switch (baseline) {
            case PROJECT:
                return Messages.Baseline_PROJECT();
            case MODIFIED_LINES:
                return Messages.Baseline_MODIFIED_LINES();
            case PROJECT_DELTA:
                return Messages.Baseline_PROJECT_DELTA();
            default:
                throw new NoSuchElementException("No display name found for baseline " + baseline);
        }
    }

    public String getMetricLineDisplayName() {
        return Messages.Metric_LINE();
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

    private void add(final ListBoxModel options, final QualityGate.QualityGateCriticality criticality) {
        options.add(getDisplayName(criticality), criticality.name());
    }
}
