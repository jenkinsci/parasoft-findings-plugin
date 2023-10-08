package com.parasoft.findings.jenkins.coverage.api.metrics.model;

import java.util.function.BiFunction;

import org.jvnet.localizer.Localizable;

import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider.DisplayColors;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.CoverageLevel;

/**
 * The baseline for the code coverage computation.
 */
public enum Baseline {
    /**
     * Coverage of the whole project. This is an absolute value that might not change much from build to build.
     */
    PROJECT(Messages._Baseline_PROJECT(), "overview", CoverageLevel::getDisplayColorsOfCoverageLevel),
    /**
     * Coverage of the modified lines (e.g., within the modified lines of a pull or merge request) will focus on new or
     * modified code only.
     */
    MODIFIED_LINES(Messages._Baseline_MODIFIED_LINES(), "modifiedLinesCoverage", CoverageLevel::getDisplayColorsOfCoverageLevel);

    private final Localizable title;
    private final String url;
    private final BiFunction<Double, ColorProvider, DisplayColors> colorMapper;

    Baseline(final Localizable title, final String url,
            final BiFunction<Double, ColorProvider, DisplayColors> colorMapper) {
        this.title = title;
        this.url = url;
        this.colorMapper = colorMapper;
    }

    public String getTitle() {
        return title.toString();
    }

    public String getUrl() {
        return "#" + url;
    }

    /**
     * Returns the display colors to use render a value of this baseline.
     *
     * @param value
     *         the value to render
     * @param colorProvider
     *         the color provider to use
     *
     * @return the display colors to use
     */
    public DisplayColors getDisplayColors(final double value, final ColorProvider colorProvider) {
        return colorMapper.apply(value, colorProvider);
    }
}
