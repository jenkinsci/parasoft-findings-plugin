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

package com.parasoft.findings.jenkins.coverage.api.metrics.color;

import java.awt.*;

import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider.DisplayColors;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for {@link CoverageLevel}.
 *
 * @author Florian Orendi
 */
class CoverageLevelTest {

    private static final ColorProvider COLOR_PROVIDER = ColorProviderFactory.createDefaultColorProvider();

    @Test
    void shouldHaveWorkingGetters() {
        CoverageLevel coverageLevel = CoverageLevel.LVL_0;
        assertThat(coverageLevel.getLevel()).isEqualTo(0.0);
        assertThat(coverageLevel.getColorizationId()).isEqualTo(ColorId.INSUFFICIENT);
    }

    @Test
    void shouldGetDisplayColorsOfCoveragePercentage() {
        Color blendedColor = ColorProvider.blendColors(
                COLOR_PROVIDER.getDisplayColorsOf(CoverageLevel.LVL_60.getColorizationId()).getFillColor(),
                COLOR_PROVIDER.getDisplayColorsOf(CoverageLevel.LVL_70.getColorizationId()).getFillColor());

        assertThat(CoverageLevel.getDisplayColorsOfCoverageLevel(65.0, COLOR_PROVIDER))
                .isEqualTo(new DisplayColors(COLOR_PROVIDER.getDisplayColorsOf(ColorId.BLACK).getFillColor(),
                        blendedColor));
        assertThat(CoverageLevel.getDisplayColorsOfCoverageLevel(96.0, COLOR_PROVIDER))
                .isEqualTo(COLOR_PROVIDER.getDisplayColorsOf(ColorId.EXCELLENT));
        assertThat(CoverageLevel.getDisplayColorsOfCoverageLevel(50.0, COLOR_PROVIDER))
                .isEqualTo(COLOR_PROVIDER.getDisplayColorsOf(ColorId.VERY_BAD));
        assertThat(CoverageLevel.getDisplayColorsOfCoverageLevel(-2.0, COLOR_PROVIDER))
                .isEqualTo(COLOR_PROVIDER.getDisplayColorsOf(ColorId.WHITE));
    }
}
