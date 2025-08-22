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

package com.parasoft.findings.jenkins.coverage.api.metrics.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.coverage.model.Coverage;
import com.parasoft.findings.jenkins.coverage.model.Coverage.CoverageBuilder;
import com.parasoft.findings.jenkins.coverage.model.Metric;
import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.ChartModelConfiguration.AxisType;
import edu.hm.hafner.echarts.line.LinesChartModel;
import edu.hm.hafner.echarts.line.LinesDataSet;
import edu.hm.hafner.util.ResourceTest;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link CoverageSeriesBuilder}.
 *
 * @author Ullrich Hafner
 */
class CoverageSeriesBuilderTest extends ResourceTest {

    @Test
    void shouldHaveEmptyDataSetForEmptyIterator() {
        CoverageSeriesBuilder builder = new CoverageSeriesBuilder();

        LinesDataSet model = builder.createDataSet(createConfiguration(), new ArrayList<>());

        assertThat(model.getDomainAxisSize()).isEqualTo(0);
        assertThat(model.getDataSetIds()).isEmpty();
    }

    @Test
    void shouldCreateChart() {
        CoverageTrendChart trendChart = new CoverageTrendChart();

        BuildResult<CoverageStatistics> smallLineCoverage = createResult(1,
                new CoverageBuilder().setMetric(Metric.LINE).setCovered(1).setMissed(1).build(),
                new CoverageBuilder().setMetric(Metric.BRANCH).setCovered(3).setMissed(1).build());

        LinesChartModel lineCoverage = trendChart.create(Collections.singletonList(smallLineCoverage),
                createConfiguration());
        verifySeriesDetails(lineCoverage);
    }

    @Test
    void shouldCreateChart_withVerySmallCoverageTrend() {
        BuildResult<CoverageStatistics> first = createResult(1,
                new CoverageBuilder().setMetric(Metric.LINE).setCovered(20).setMissed(980).build(),
                new CoverageBuilder().setMetric(Metric.BRANCH).setCovered(3).setMissed(1).build());
        BuildResult<CoverageStatistics> second = createResult(2,
                new CoverageBuilder().setMetric(Metric.LINE).setCovered(21).setMissed(979).build(),
                new CoverageBuilder().setMetric(Metric.BRANCH).setCovered(3).setMissed(1).build());

        CoverageTrendChart trendChart = new CoverageTrendChart();
        var lineCoverage = trendChart.create(List.of(first, second), createConfiguration());
        assertThat(lineCoverage.getRangeMin()).isEqualTo(1.8);
        assertThat(lineCoverage.getRangeMax()).isEqualTo(2.3);
    }

    private BuildResult<CoverageStatistics> createResult(final int buildNumber,
            final Coverage lineCoverage, final Coverage branchCoverage) {
        var statistics = new CoverageStatistics(
                List.of(lineCoverage, branchCoverage), Collections.emptyList());
        Build build = new Build(buildNumber);

        return new BuildResult<>(build, statistics);
    }

    private void verifySeriesDetails(final LinesChartModel lineCoverage) {
        assertThat(lineCoverage.getBuildNumbers()).containsExactly(1);
        assertThat(lineCoverage.getSeries()).hasSize(1);
        assertThat(lineCoverage.getRangeMax()).isEqualTo(55.0);
        assertThat(lineCoverage.getRangeMin()).isEqualTo(45.0);
    }

    @Test
    void shouldHaveTwoValuesForSingleBuild() {
        CoverageSeriesBuilder builder = new CoverageSeriesBuilder();

        BuildResult<CoverageStatistics> singleResult = createResult(1,
                new CoverageBuilder().setMetric(Metric.LINE).setCovered(1).setMissed(1).build(),
                new CoverageBuilder().setMetric(Metric.BRANCH).setCovered(3).setMissed(1).build());

        LinesDataSet dataSet = builder.createDataSet(createConfiguration(), Collections.singletonList(singleResult));

        assertThat(dataSet.getDomainAxisSize()).isEqualTo(1);
        assertThat(dataSet.getDomainAxisLabels()).containsExactly("#1");

        assertThat(dataSet.getDataSetIds()).containsExactlyInAnyOrder(
                CoverageSeriesBuilder.LINE_COVERAGE);

        assertThat(dataSet.getSeries(CoverageSeriesBuilder.LINE_COVERAGE)).containsExactly(50.0);
    }

    @Test
    void shouldHaveTwoValuesForTwoBuilds() {
        CoverageSeriesBuilder builder = new CoverageSeriesBuilder();

        BuildResult<CoverageStatistics> first = createResult(1,
                new CoverageBuilder().setMetric(Metric.LINE).setCovered(1).setMissed(1).build(),
                new CoverageBuilder().setMetric(Metric.BRANCH).setCovered(3).setMissed(1).build());
        BuildResult<CoverageStatistics> second = createResult(2,
                new CoverageBuilder().setMetric(Metric.LINE).setCovered(1).setMissed(3).build(),
                new CoverageBuilder().setMetric(Metric.BRANCH).setCovered(1).setMissed(3).build());

        LinesDataSet dataSet = builder.createDataSet(createConfiguration(), List.of(first, second));

        assertThat(dataSet.getDomainAxisSize()).isEqualTo(2);
        assertThat(dataSet.getDomainAxisLabels()).containsExactly("#1", "#2");

        assertThat(dataSet.getDataSetIds()).containsExactlyInAnyOrder(
                CoverageSeriesBuilder.LINE_COVERAGE);

        assertThat(dataSet.getSeries(CoverageSeriesBuilder.LINE_COVERAGE))
                .containsExactly(50.0, 25.0);

        CoverageTrendChart trendChart = new CoverageTrendChart();
        var model = trendChart.create(List.of(first, second), createConfiguration());

        assertThatJson(model).isEqualTo(toString("chart.json"));
    }

    private ChartModelConfiguration createConfiguration() {
        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        return configuration;
    }
}
