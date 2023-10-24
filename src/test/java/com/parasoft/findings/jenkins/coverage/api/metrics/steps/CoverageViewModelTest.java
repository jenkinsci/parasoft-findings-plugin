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

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.coverage.model.Node;
import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;

import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageViewModel.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link CoverageViewModel}.
 *
 * @author Ullrich Hafner
 * @author Florian Orendi
 */
@SuppressWarnings("PMD.TooManyStaticImports")
class CoverageViewModelTest extends AbstractCoverageTest {
    @Test
    void shouldReturnEmptySourceViewForExistingLinkButMissingSourceFile() {
        CoverageViewModel model = createModelFromCodingStyleReport();

        String hash = String.valueOf("PathUtil.java".hashCode());
        assertThat(model.getSourceCode(hash, ABSOLUTE_COVERAGE_TABLE_ID)).isEqualTo("N/A");
        assertThat(model.getSourceCode(hash, MODIFIED_LINES_COVERAGE_TABLE_ID)).isEqualTo("N/A");
    }

    @Test
    void shouldReportOverview() {
        CoverageViewModel model = createModelFromCodingStyleReport();

        CoverageOverview overview = model.getOverview();

        var expectedMetrics = new String[] {"Package", "File", "Class", "Method", "Line", "Branch", "Instruction"};
        assertThat(overview.getMetrics()).containsExactly(expectedMetrics);

        var expectedCovered = List.of(4, 7, 15, 97, 294, 109, 1260);
        assertThat(overview.getCovered()).containsExactlyElementsOf(expectedCovered);
        ensureValidPercentages(overview.getCoveredPercentages());

        var expectedMissed = List.of(0, 3, 3, 5, 29, 7, 90);
        assertThat(overview.getMissed()).containsExactlyElementsOf(expectedMissed);
        ensureValidPercentages(overview.getMissedPercentages());

        assertThatJson(overview).node("metrics").isArray().containsExactly(expectedMetrics);
        assertThatJson(overview).node("covered").isArray().containsExactlyElementsOf(expectedCovered);
        assertThatJson(overview).node("missed").isArray().containsExactlyElementsOf(expectedMissed);
    }

    private static void ensureValidPercentages(final List<Double> percentages) {
        assertThat(percentages).allSatisfy(d ->
                assertThat(d).isLessThanOrEqualTo(100.0).isGreaterThanOrEqualTo(0.0));
    }

    @Test
    void shouldProvideRightTableModelById() {
        CoverageViewModel model = createModelFromCodingStyleReport();
        assertThat(model.getTableModel(MODIFIED_LINES_COVERAGE_TABLE_ID)).isInstanceOf(ModifiedLinesCoverageTableModel.class);
        assertThat(model.getTableModel(ABSOLUTE_COVERAGE_TABLE_ID)).isInstanceOf(CoverageTableModel.class);

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> model.getTableModel("wrong-id"));
    }

    private CoverageViewModel createModelFromCodingStyleReport() {
        var model = createModel(readJacocoResult("jacoco-codingstyle.xml"));
        assertThat(model.getDisplayName()).contains("'Java coding style'");
        return model;
    }

    private CoverageViewModel createModel(final Node node) {
        return new CoverageViewModel(mock(Run.class), "id", StringUtils.EMPTY, node, new FilteredLog("Errors"), i -> i);
    }
}
