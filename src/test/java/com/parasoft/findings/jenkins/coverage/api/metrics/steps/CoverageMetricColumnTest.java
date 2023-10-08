package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.hm.hafner.coverage.Node;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import edu.hm.hafner.coverage.Coverage.CoverageBuilder;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Value;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;

import hudson.model.Job;
import hudson.model.Run;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import io.jenkins.plugins.util.QualityGateResult;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link CoverageMetricColumn}.
 *
 * @author Florian Orendi
 */
@DefaultLocale("en")
class CoverageMetricColumnTest extends AbstractCoverageTest {

    /**
     * Creates a stub for a {@link Job} that has the specified actions attached.
     *
     * @param actions
     *         The actions to attach, might be empty
     *
     * @return the created stub
     */
    @VisibleForTesting
    public static Job<?, ?> createJobWithActions(final CoverageBuildAction... actions) {
        Job<?, ?> job = mock(Job.class);
        Run<?, ?> build = createBuildWithActions(actions);
        when(job.getLastCompletedBuild()).thenAnswer(a -> build);
        return job;
    }

    /**
     * Creates a stub for a {@link Run} that has the specified actions attached.
     *
     * @param actions
     *         the actions to attach, might be empty
     *
     * @return the created stub
     */
    @VisibleForTesting
    public static Run<?, ?> createBuildWithActions(final CoverageBuildAction... actions) {
        Run<?, ?> build = mock(Run.class);
        when(build.getActions(CoverageBuildAction.class)).thenReturn(Arrays.asList(actions));
        if (actions.length > 0) {
            when(build.getAction(CoverageBuildAction.class)).thenReturn(actions[0]);
        }
        return build;
    }

    @Test
    void shouldHaveWorkingDataGetters() {
        CoverageMetricColumn column = createColumn();

        assertThat(column.getRelativeCoverageUrl(mock(Job.class))).isEmpty();
    }

    @Test
    void shouldProvideSelectedColumn() {
        CoverageMetricColumn column = createColumn();
        var node = readCoberturaResult(COBERTURA_CODING_STYLE_FILE);
        Job<?, ?> job = createJobWithCoverageAction(node);

        assertThat(column.getRelativeCoverageUrl(job)).isEqualTo("coverage/#overview");
    }

    @Test
    void shouldProvideBackgroundColorFillPercentage() {
        CoverageMetricColumn column = createColumn();

        assertThat(column.getBackgroundColorFillPercentage("+5,0%")).isEqualTo("100%");
        assertThat(column.getBackgroundColorFillPercentage("+5.0%")).isEqualTo("100%");
        assertThat(column.getBackgroundColorFillPercentage("5,00%")).isEqualTo("5.00%");
        assertThat(column.getBackgroundColorFillPercentage("5.00%")).isEqualTo("5.00%");
    }

    @Test
    void shouldShowNoResultIfBuild() {
        CoverageMetricColumn column = createColumn();

        Job<?, ?> job = mock(Job.class);

        assertThat(column.getCoverageText(job)).isEqualTo(Messages.Coverage_Not_Available());

        Optional<? extends Value> coverageValue = column.getCoverageValue(job);
        assertThat(coverageValue).isEmpty();
        assertThat(column.getDisplayColors(job, Optional.empty())).isEqualTo(ColorProvider.DEFAULT_COLOR);
    }

    @Test
    void shouldShowNoResultIfNoAction() {
        CoverageMetricColumn column = createColumn();

        Job<?, ?> job = createJobWithActions();

        assertThat(column.getCoverageText(job)).isEqualTo(Messages.Coverage_Not_Available());
        assertThat(column.getCoverageValue(job)).isEmpty();
        assertThat(column.getDisplayColors(job, Optional.empty())).isEqualTo(ColorProvider.DEFAULT_COLOR);
    }

    @Test
    void shouldShowNoResultForEmptyReport() {
        CoverageMetricColumn column = createColumn();
        var node = readCoberturaResult(COBERTURA_CODING_STYLE_NO_DATA_FILE);

        Job<?, ?> job = createJobWithCoverageAction(node);

        assertThat(column.getCoverageText(job)).isEqualTo(Messages.Coverage_Not_Available());
        assertThat(column.getCoverageValue(job)).isEmpty();

        assertThat(column.getCoverageText(job)).isEqualTo(Messages.Coverage_Not_Available());
        assertThat(column.getCoverageValue(job)).isEmpty();
    }

    @Test
    void shouldCalculateProjectCoverage() {
        CoverageMetricColumn column = createColumn();
        var node = readCoberturaResult(COBERTURA_CODING_STYLE_FILE);
        Job<?, ?> job = createJobWithCoverageAction(node);

        assertThat(column.getCoverageText(job)).isEqualTo("77.78%");
        assertThat(column.getCoverageValue(job))
                .isNotEmpty()
                .satisfies(coverage -> {
                    assertThat(coverage.get()).isEqualTo(new CoverageBuilder().setMetric(Metric.LINE).setCovered(28).setMissed(8).build());
                    assertThat(column.getDisplayColors(job, coverage).getLineColor())
                            .isEqualTo(Color.black);
                });
    }

    private CoverageMetricColumn createColumn() {
        CoverageMetricColumn column = new CoverageMetricColumn();
        return column;
    }

    private Job<?, ?> createJobWithCoverageAction(Node node) {
        var run = mock(Run.class);
        CoverageBuildAction coverageBuildAction =
                new CoverageBuildAction(run, "coverage", "Code Coverage",
                        node, new QualityGateResult(), new FilteredLog("Test"),
                        "-", List.of(), false);
        when(run.getAction(CoverageBuildAction.class)).thenReturn(coverageBuildAction);
        when(run.getActions(CoverageBuildAction.class)).thenReturn(Collections.singletonList(coverageBuildAction));

        var job = mock(Job.class);
        when(job.getLastCompletedBuild()).thenReturn(run);

        return job;
    }
}
