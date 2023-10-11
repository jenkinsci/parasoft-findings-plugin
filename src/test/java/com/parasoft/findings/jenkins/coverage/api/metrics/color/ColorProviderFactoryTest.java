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
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.parasoft.findings.jenkins.coverage.api.metrics.color.CoverageColorJenkinsId.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Test class for {@link ColorProviderFactory}.
 *
 * @author Florian Orendi
 */
class ColorProviderFactoryTest {

    private static final String TEST_COLOR_HEX = "#ffffff";
    private static final Color TEST_COLOR = Color.decode(TEST_COLOR_HEX);

    @Test
    void shouldCreateDefaultColorProvider() {
        ColorProvider colorProvider = ColorProviderFactory.createDefaultColorProvider();
        for (CoverageColorPalette color : CoverageColorPalette.values()) {
            assertThat(colorProvider.containsColorId(color.getColorId())).isTrue();
        }
    }

    @Test
    void shouldCreateColorProviderWithJenkinsColors() {
        Map<String, String> colorMapping = createColorMapping();
        ColorProvider colorProvider = ColorProviderFactory.createColorProvider(colorMapping);

        for (CoverageColorPalette color : CoverageColorPalette.values()) {
            assertThat(colorProvider.containsColorId(color.getColorId())).isTrue();
            if (!color.getColorId().equals(ColorId.BLACK) && !color.getColorId()
                    .equals(ColorId.WHITE)) { // skip set default color
                assertThat(colorProvider.getDisplayColorsOf(color.getColorId()))
                        .satisfies(displayColor -> assertThat(displayColor.getFillColor()).isEqualTo(TEST_COLOR));
            }
        }
    }

    @Test
    void shouldCreateDefaultColorProviderWithMissingJenkinsColorIds() {
        Map<String, String> colorMapping = createColorMapping();
        colorMapping.remove("--green");
        ColorProvider colorProvider = ColorProviderFactory.createColorProvider(colorMapping);
        for (CoverageColorPalette color : CoverageColorPalette.values()) {
            assertThat(colorProvider.containsColorId(color.getColorId())).isTrue();
        }
    }

    @Test
    void shouldCreateDefaultColorProviderWithoutHexColors() {
        Map<String, String> colorMapping = createColorMapping();
        colorMapping.replace("--green", "hsl(135deg, 75%, 55%)");
        ColorProvider colorProvider = ColorProviderFactory.createColorProvider(colorMapping);
        for (CoverageColorPalette color : CoverageColorPalette.values()) {
            assertThat(colorProvider.containsColorId(color.getColorId())).isTrue();
        }
    }

    /**
     * Creates a color mapping between the {@link CoverageColorJenkinsId jenkins color id} and the corresponding color
     * hex code.
     *
     * @return the created mapping
     */
    private Map<String, String> createColorMapping() {
        Map<String, String> colorMapping = new HashMap<>();
        getAll().forEach(id -> colorMapping.put(id, TEST_COLOR_HEX));
        return colorMapping;
    }
}
