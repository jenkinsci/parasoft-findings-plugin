package com.parasoft.findings.jenkins.coverage.api.metrics.model;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.Fraction;

import edu.hm.hafner.coverage.FractionValue;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Value;

/**
 * Represents the different mappings of coverage metric and baseline to actual values.
 */
public class CoverageStatistics {
    private final List<Value> projectValueMapping;
    private final List<Value> changeValueMapping;

    /**
     * Creates a new instance of {@link CoverageStatistics}.
     *
     * @param projectValueMapping
     *         mapping of metrics to values for {@link Baseline#PROJECT}
     * @param modifiedLinesValueMapping
     *         mapping of metrics to values for {@link Baseline#MODIFIED_LINES}
     */
    public CoverageStatistics(
            final List<? extends Value> projectValueMapping,
            final List<? extends Value> modifiedLinesValueMapping) {
        this.projectValueMapping = List.copyOf(projectValueMapping);
        this.changeValueMapping = List.copyOf(modifiedLinesValueMapping);
    }

    private static NavigableMap<Metric, Value> asValueMap(final NavigableMap<Metric, Fraction> projectDelta) {
        return projectDelta.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, e -> new FractionValue(e.getKey(), e.getValue()), (o1, o2) -> o1,
                        TreeMap::new));
    }

    /**
     * Returns the value for the specified baseline and metric.
     *
     * @param baseline
     *         the baseline of the value
     * @param metric
     *         the metric of the value
     *
     * @return the value, if available
     */
    public Optional<Value> getValue(final Baseline baseline, final Metric metric) {
        if (baseline == Baseline.PROJECT) {
            return Value.findValue(metric, projectValueMapping);
        }
        if (baseline == Baseline.MODIFIED_LINES) {
            return Value.findValue(metric, changeValueMapping);
        }
        throw new NoSuchElementException("No such baseline: " + baseline);
    }

    private Optional<Value> getValue(final Metric metric, final NavigableMap<Metric, Value> mapping) {
        return Optional.ofNullable(mapping.get(metric));
    }

    /**
     * Returns whether a value for the specified metric and baseline is available.
     *
     * @param baseline
     *         the baseline of the value
     * @param metric
     *         the metric of the value
     *
     * @return {@code true}, if a value is available, {@code false} otherwise
     */
    public boolean containsValue(final Baseline baseline, final Metric metric) {
        return getValue(baseline, metric).isPresent();
    }
}
