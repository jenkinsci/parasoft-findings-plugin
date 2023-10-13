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

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.line.LinesChartModel;
import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.model.Job;

import com.parasoft.findings.jenkins.coverage.api.metrics.charts.CoverageTrendChart;
import io.jenkins.plugins.echarts.ActionSelector;
import io.jenkins.plugins.echarts.GenericBuildActionIterator.BuildActionIterable;
import io.jenkins.plugins.echarts.TrendChartJobAction;

/**
 * Project level action for the coverage results. A job action displays a link on the side panel of a job that refers to
 * the last build that contains coverage results (i.e. a {@link CoverageBuildAction} with a {@link Node} instance). This
 * action also is responsible to render the historical trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class CoverageJobAction extends TrendChartJobAction<CoverageBuildAction> {
    private final String id;
    private final String name;
    private final String icon;

    CoverageJobAction(final Job<?, ?> owner, final String id, final String name, final String icon) {
        super(owner, CoverageBuildAction.class);

        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getIconFileName() {
        return icon;
    }

    @Override
    public String getDisplayName() {
        return StringUtils.defaultIfBlank(name, Messages.Coverage_Link_Name());
    }

    /**
     * Returns a label for the trend chart.
     *
     * @return a label for the trend chart
     */
    public String getTrendName() {
        if (StringUtils.isBlank(name)) {
            return Messages.Coverage_Trend_Default_Name();
        }
        return Messages.Coverage_Trend_Name(name);
    }

    @Override @NonNull
    public String getUrlName() {
        return id;
    }

    public Job<?, ?> getProject() {
        return getOwner();
    }

    public String getSearchUrl() {
        return getUrlName();
    }

    @Override
    protected LinesChartModel createChartModel(final String configuration) {
        var iterable = new BuildActionIterable<>(CoverageBuildAction.class, getLatestAction(),
                selectByUrl(), CoverageBuildAction::getStatistics);

        return new CoverageTrendChart().create(iterable, ChartModelConfiguration.fromJson(configuration));
    }

    @Override
    public Optional<CoverageBuildAction> getLatestAction() {
        return new ActionSelector<>(CoverageBuildAction.class, selectByUrl()).findFirst(getOwner().getLastBuild());
    }

    private Predicate<CoverageBuildAction> selectByUrl() {
        return action -> getUrlName().equals(action.getUrlName());
    }
}
