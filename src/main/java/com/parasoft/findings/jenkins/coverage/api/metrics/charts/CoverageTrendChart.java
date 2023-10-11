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

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.line.LineSeries;
import edu.hm.hafner.echarts.line.LineSeries.FilledMode;
import edu.hm.hafner.echarts.line.LineSeries.StackedMode;
import edu.hm.hafner.echarts.line.LinesChartModel;
import edu.hm.hafner.echarts.line.LinesDataSet;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.CoverageStatistics;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Messages;
import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the Java side model for a trend chart showing the line and branch coverage of a project. The number of builds
 * to consider is controlled by a {@link ChartModelConfiguration} instance. The created model object can be serialized
 * to JSON (e.g., using the {@link JacksonFacade}) and can be used 1:1 as ECharts configuration object in the
 * corresponding JS file.
 *
 * @author Ullrich Hafner
 * @see JacksonFacade
 */
public class CoverageTrendChart {
    private static final String LINE_COVERAGE_COLOR = JenkinsPalette.GREEN.normal();
    private static final String BRANCH_COVERAGE_COLOR = JenkinsPalette.GREEN.dark();

    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the forensics results to render - these results must be provided in descending order, i.e. the current *
     *         build is the head of the list, then the previous builds, and so on
     * @param configuration
     *         the chart configuration to be used
     *
     * @return the chart model, ready to be serialized to JSON
     */
    public LinesChartModel create(final Iterable<BuildResult<CoverageStatistics>> results,
            final ChartModelConfiguration configuration) {
        CoverageSeriesBuilder builder = new CoverageSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(dataSet);
        if (dataSet.isNotEmpty()) {
            LineSeries lineSeries = new LineSeries(Messages.Metric_LINE(),
                    LINE_COVERAGE_COLOR, StackedMode.SEPARATE_LINES, FilledMode.FILLED,
                    dataSet.getSeries(CoverageSeriesBuilder.LINE_COVERAGE));
            model.addSeries(lineSeries);
            model.useContinuousRangeAxis();
            model.setRangeMax(100);
            model.setRangeMin(dataSet.getMinimumValue());
        }
        return model;
    }
}
