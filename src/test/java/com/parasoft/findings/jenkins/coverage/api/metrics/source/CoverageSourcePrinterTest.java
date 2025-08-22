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

package com.parasoft.findings.jenkins.coverage.api.metrics.source;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import com.parasoft.findings.jenkins.coverage.model.parser.JacocoParser;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;

import static org.assertj.core.api.Assertions.*;

class CoverageSourcePrinterTest extends AbstractCoverageTest {

    private static final String CLASS = "class";
    private static final String RENDERED_CODE = "                    "
            + "for (int line = 0; line < lines.size(); line++) {";

    @Test
    void shouldRenderLinesWithVariousCoverages() {
        var tree = readResult("../steps/jacoco-codingstyle.xml", new JacocoParser());

        var file = new CoverageSourcePrinter(tree.findFile("TreeStringBuilder.java").orElseThrow());

        assertThat(file.getColorClass(0)).isEqualTo(CoverageSourcePrinter.NO_COVERAGE);
        assertThat(file.getSummaryColumn(0)).isEqualTo("0");
        assertThat(file.getTooltip(0)).isEqualTo("Not covered");

        assertThat(file.getColorClass(113)).isEqualTo(CoverageSourcePrinter.PARTIAL_COVERAGE);
        assertThat(file.getSummaryColumn(113)).isEqualTo("1/2");
        assertThat(file.getTooltip(113)).isEqualToIgnoringWhitespace("Partially covered, branch coverage: 1/2");

        assertThat(file.getColorClass(61)).isEqualTo(CoverageSourcePrinter.NO_COVERAGE);
        assertThat(file.getSummaryColumn(61)).isEqualTo("0");
        assertThat(file.getTooltip(61)).isEqualTo("Not covered");

        assertThat(file.getColorClass(19)).isEqualTo(CoverageSourcePrinter.FULL_COVERAGE);
        assertThat(file.getSummaryColumn(19)).isEqualTo("1");
        assertThat(file.getTooltip(19)).isEqualTo("Covered at least once");

        var anotherFile = new CoverageSourcePrinter(tree.findFile("StringContainsUtils.java").orElseThrow());

        assertThat(anotherFile.getColorClass(43)).isEqualTo(CoverageSourcePrinter.FULL_COVERAGE);
        assertThat(anotherFile.getSummaryColumn(43)).isEqualTo("2/2");
        assertThat(anotherFile.getTooltip(43)).isEqualTo("All branches covered");
    }

    @Test
    void shouldRenderWholeLine() {
        var tree = readResult("../steps/jacoco-codingstyle.xml", new JacocoParser());

        var file = new CoverageSourcePrinter(tree.findFile("TreeStringBuilder.java").orElseThrow());

        var renderedLine = file.renderLine(61,
                "                    for (int line = 0; line < lines.size(); line++) {\n");

        XmlAssert.assertThat(renderedLine)
                .nodesByXPath("/tr").exist().hasSize(1)
                .singleElement()
                .hasAttribute(CLASS, CoverageSourcePrinter.NO_COVERAGE)
                .hasAttribute("data-html-tooltip", "Not covered");
        var assertThatColumns = XmlAssert.assertThat(renderedLine).nodesByXPath("/tr/td").exist().hasSize(3);
        assertThatColumns.extractingAttribute("class").containsExactly("line", "hits", "code");

        XmlAssert.assertThat(renderedLine).nodesByXPath("/tr/td[1]/a").exist().hasSize(1)
                .extractingAttribute("name").containsExactly("61");
        XmlAssert.assertThat(renderedLine).nodesByXPath("/tr/td[2]")
                .extractingText().containsExactly("0");
        XmlAssert.assertThat(renderedLine).nodesByXPath("/tr/td[3]")
                .extractingText().containsExactly(RENDERED_CODE);

        var skippedLine = file.renderLine(1, "package com.parasoft.findings.jenkins.coverage.api.metrics.source;");

        var assertThatSkippedColumns = XmlAssert.assertThat(renderedLine).nodesByXPath("/tr/td").exist().hasSize(3);
        assertThatSkippedColumns.extractingAttribute("class").containsExactly("line", "hits", "code");

        XmlAssert.assertThat(skippedLine)
                .nodesByXPath("/tr").exist().hasSize(1)
                .singleElement()
                .hasAttribute(CLASS, CoverageSourcePrinter.UNDEFINED)
                .doesNotHaveAttribute("data-html-tooltip");

        XmlAssert.assertThat(skippedLine).nodesByXPath("/tr/td[1]/a").exist().hasSize(1)
                .extractingAttribute("name").containsExactly("1");
        XmlAssert.assertThat(skippedLine).nodesByXPath("/tr/td[2]")
                .extractingText().containsExactly(StringUtils.EMPTY);
        XmlAssert.assertThat(skippedLine).nodesByXPath("/tr/td[3]")
                .extractingText().containsExactly("package com.parasoft.findings.jenkins.coverage.api.metrics.source;");

    }
}
