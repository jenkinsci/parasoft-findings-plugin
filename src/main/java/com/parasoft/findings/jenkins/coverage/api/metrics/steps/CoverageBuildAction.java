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

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.coverage.Value;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Run;

import com.parasoft.findings.jenkins.coverage.api.metrics.charts.CoverageTrendChart;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.ElementFormatter;
import io.jenkins.plugins.echarts.GenericBuildActionIterator.BuildActionIterable;
import io.jenkins.plugins.forensics.reference.ReferenceBuild;
import io.jenkins.plugins.util.AbstractXmlStream;
import io.jenkins.plugins.util.BuildAction;
import io.jenkins.plugins.util.QualityGateResult;

import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.DEFAULT_REFERENCE_BUILD_IDENTIFIER;
import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.ReferenceStatus.*;
import static hudson.model.Run.*;

/**
 * Controls the life cycle of the coverage results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file. This action also provides access to the coverage details: these are rendered using a new view instance.
 */
@SuppressWarnings({"PMD.GodClass", "checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
public final class CoverageBuildAction extends BuildAction<Node> implements StaplerProxy {
    private static final long serialVersionUID = -6023811049340671399L;

    private static final ElementFormatter FORMATTER = new ElementFormatter();
    private static final String NO_REFERENCE_BUILD = "-";

    private final String id;
    private final String name;

    private final String referenceBuildId;

    private final QualityGateResult qualityGateResult;

    private final String icon;
    private final FilteredLog log;

    /** The aggregated values of the result for the root of the tree. */
    private final List<? extends Value> projectValues;

    /** The coverages filtered by modified lines of the associated change request. */
    private final List<? extends Value> modifiedLinesCoverage;

    private final ReferenceResult referenceResult;

    static {
        CoverageXmlStream.registerConverters(XSTREAM2);
    }

    /**
     * Creates a new instance of {@link CoverageBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param id
     *         ID (URL) of the results
     * @param optionalName
     *         optional name that overrides the default name of the results
     * @param icon
     *         name of the icon that should be used in actions and views
     * @param result
     *         the coverage tree as a result to persist with this action
     * @param qualityGateResult
     *         status of the quality gates
     * @param log
     *         the logging statements of the recording step
     */
    public CoverageBuildAction(final Run<?, ?> owner, final String id, final String optionalName, final String icon,
            final Node result, final QualityGateResult qualityGateResult, final FilteredLog log, final ReferenceResult referenceResult) {
        this(owner, id, optionalName, icon, result, qualityGateResult, log, NO_REFERENCE_BUILD,
                List.of(), referenceResult);
    }

    /**
     * Creates a new instance of {@link CoverageBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param id
     *         ID (URL) of the results
     * @param optionalName
     *         optional name that overrides the default name of the results
     * @param icon
     *         name of the icon that should be used in actions and views
     * @param result
     *         the coverage tree as a result to persist with this action
     * @param qualityGateResult
     *         status of the quality gates
     * @param log
     *         the logging statements of the recording step
     * @param referenceBuildId
     *         the ID of the reference build
     * @param modifiedLinesCoverage
     *         the coverages filtered by modified lines of the associated change request
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public CoverageBuildAction(final Run<?, ?> owner, final String id, final String optionalName, final String icon,
            final Node result, final QualityGateResult qualityGateResult, final FilteredLog log,
            final String referenceBuildId,
            final List<? extends Value> modifiedLinesCoverage,
            final ReferenceResult referenceResult) {
        this(owner, id, optionalName, icon, result, qualityGateResult, log, referenceBuildId,
                modifiedLinesCoverage, true, referenceResult);
    }

    @VisibleForTesting
    @SuppressWarnings("checkstyle:ParameterNumber")
    CoverageBuildAction(final Run<?, ?> owner, final String id, final String name, final String icon,
            final Node result, final QualityGateResult qualityGateResult, final FilteredLog log,
            final String referenceBuildId,
            final List<? extends Value> modifiedLinesCoverage,
            final boolean canSerialize,
            final ReferenceResult referenceResult) {
        super(owner, result, false);

        this.id = id;
        this.name = name;
        this.icon = icon;
        this.log = log;

        projectValues = result.aggregateValues();
        this.qualityGateResult = qualityGateResult;
        this.modifiedLinesCoverage = new ArrayList<>(modifiedLinesCoverage);
        this.referenceBuildId = referenceBuildId;
        this.referenceResult = referenceResult;

        if (canSerialize) {
            createXmlStream().write(owner.getRootDir().toPath().resolve(getBuildResultBaseName()), result);
        }
    }

    /**
     * Returns the actual name of the tool. If no user-defined name is given, then the default name is returned.
     *
     * @return the name
     */
    private String getActualName() {
        return StringUtils.defaultIfBlank(name, Messages.Coverage_Link_Name());
    }

    public FilteredLog getLog() {
        return log;
    }

    public QualityGateResult getQualityGateResult() {
        return qualityGateResult;
    }

    public ElementFormatter getFormatter() {
        return FORMATTER;
    }

    public CoverageStatistics getStatistics() {
        return new CoverageStatistics(projectValues, modifiedLinesCoverage);
    }

    /**
     * Returns the supported baselines.
     *
     * @return all supported baselines
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Baseline getProjectBaseline() {
        return Baseline.PROJECT;
    }

    /**
     * Returns whether a delta metric for the specified metric exists.
     *
     * @param baseline
     *         the baseline to use
     *
     * @return {@code true} if a delta is available for the specified metric, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean hasBaselineResult(final Baseline baseline) {
        return !getValues(baseline).isEmpty();
    }


    /**
     * Returns the title text for the specified baseline.
     *
     * @param baseline
     *         the baseline to get the title for
     *
     * @return the title
     */
    public String getTitle(final Baseline baseline) {
        return baseline.getTitle();
    }

    /**
     * Returns all available values for the specified baseline.
     *
     * @param baseline
     *         the baseline to get the values for
     *
     * @return the available values
     * @throws NoSuchElementException
     *         if this baseline does not provide values
     */
    public List<Value> getAllValues(final Baseline baseline) {
        return getValueStream(baseline).collect(Collectors.toList());
    }

    /**
     * Returns all important values for the specified baseline.
     *
     * @param baseline
     *         the baseline to get the values for
     *
     * @return the available values
     * @throws NoSuchElementException
     *         if this baseline does not provide values
     */
    public List<Value> getValues(final Baseline baseline) {
        return filterImportantMetrics(getValueStream(baseline));
    }

    /**
     * Returns the value for the specified metric, if available.
     *
     * @param baseline
     *         the baseline to get the value for
     * @param metric
     *         the metric to get the value for
     *
     * @return the optional value
     */
    public Optional<Value> getValueForMetric(final Baseline baseline, final Metric metric) {
        return getAllValues(baseline).stream()
                .filter(value -> value.getMetric() == metric)
                .findFirst();
    }

    private List<Value> filterImportantMetrics(final Stream<? extends Value> values) {
        return values.filter(v -> getMetricsForSummary().contains(v.getMetric()))
                .collect(Collectors.toList());
    }

    private Stream<? extends Value> getValueStream(final Baseline baseline) {
        if (baseline == Baseline.PROJECT) {
            return projectValues.stream();
        }
        if (baseline == Baseline.MODIFIED_LINES) {
            return modifiedLinesCoverage.stream();
        }
        throw new NoSuchElementException("No such baseline: " + baseline);
    }

    /**
     * Returns a formatted and localized String representation of the value for the specified metric (with respect to
     * the given baseline).
     *
     * @param baseline
     *         the baseline to use
     * @param metric
     *         the metric to get the delta for
     *
     * @return the formatted value
     */
    public String formatValue(final Baseline baseline, final Metric metric) {
        var value = getValueForMetric(baseline, metric);
        return value.isPresent() ? FORMATTER.formatValue(value.get()) : Messages.Coverage_Not_Available();
    }

    /**
     * Returns the visible metrics for the project summary.
     *
     * @return the metrics to be shown in the project summary
     */
    @VisibleForTesting
    NavigableSet<Metric> getMetricsForSummary() {
        return new TreeSet<>(
                Set.of(Metric.LINE, Metric.LOC));
    }

    /**
     * Renders the reference build as HTML-link.
     *
     * @return the reference build
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getReferenceBuildLink() {
        String referenceBuildLink = ReferenceBuild.getReferenceBuildLink(referenceBuildId);
        if (referenceBuildLink.equals(String.format("#%s", referenceBuildId))) {
            return String.format("%s%s",referenceBuildId, Messages.Reference_Build_Removed());
        }
        return referenceBuildLink;
    }

    @SuppressWarnings("unused")// Called by jelly view
    public String getReferenceBuildWarningMessage() {
        ReferenceResult.ReferenceStatus status = referenceResult.getStatus();
        String referenceBuild = referenceResult.getReferenceBuild();
        if (status == NO_REF_BUILD) {
            if (referenceBuild.equals(DEFAULT_REFERENCE_BUILD_IDENTIFIER)) {
                return Messages.Reference_Build_Warning_Message_NO_REF_BUILD();
            }
            return Messages.Reference_Build_Warning_Message_NO_SPECIFIED_REF_BUILD(referenceBuild);
        } else if (status == NO_CVG_DATA_IN_REF_BUILD) {
            if (referenceBuild.equals(DEFAULT_REFERENCE_BUILD_IDENTIFIER)) {
                return Messages.Reference_Build_Warning_Message_NO_CVG_DATA_IN_PREVIOUS_SUCCESSFUL_BUILDS();
            }
            return Messages.Reference_Build_Warning_Message_NO_CVG_DATA_IN_REF_BUILD(referenceBuild);
        } else if (status == REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE) {
            return Messages.Reference_Build_Warning_Message_REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE(referenceBuild);
        } else if (status == NO_PREVIOUS_BUILD_WAS_FOUND) {
            return Messages.Reference_Build_Warning_Message_NO_PREVIOUS_BUILD_WAS_FOUND();
        }
        return "";
    }

    @Override
    protected AbstractXmlStream<Node> createXmlStream() {
        return new CoverageXmlStream();
    }

    @Override
    protected CoverageJobAction createProjectAction() {
        return new CoverageJobAction(getOwner().getParent(), getUrlName(), name, icon);
    }

    @Override
    protected String getBuildResultBaseName() {
        return String.format("%s.xml", id);
    }

    @Override
    public CoverageViewModel getTarget() {
        return new CoverageViewModel(getOwner(), getUrlName(), name, getResult(), log, this::createChartModel);
    }

    private String createChartModel(final String configuration) {
        var iterable = new BuildActionIterable<>(CoverageBuildAction.class, Optional.of(this),
                action -> getUrlName().equals(action.getUrlName()), CoverageBuildAction::getStatistics);
        return new JacksonFacade().toJson(
                new CoverageTrendChart().create(iterable, ChartModelConfiguration.fromJson(configuration)));
    }

    @NonNull
    @Override
    public String getIconFileName() {
        return icon;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return getActualName();
    }

    @NonNull
    @Override
    public String getUrlName() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", getDisplayName(), getUrlName(), projectValues);
    }
}
