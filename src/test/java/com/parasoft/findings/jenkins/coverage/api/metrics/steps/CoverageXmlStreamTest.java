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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com.parasoft.findings.jenkins.coverage.model.Metric;
import com.parasoft.findings.jenkins.coverage.model.Node;
import com.parasoft.findings.jenkins.coverage.model.Value;
import com.parasoft.findings.jenkins.coverage.model.parser.JacocoParser;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.SerializableTest;

import hudson.XmlFile;
import hudson.model.FreeStyleBuild;
import hudson.util.XStream2;

import com.parasoft.findings.jenkins.coverage.Assertions;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageXmlStream.IntegerLineMapConverter;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageXmlStream.IntegerSetConverter;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageXmlStream.MetricFractionMapConverter;
import io.jenkins.plugins.util.QualityGateResult;

import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.DEFAULT_REFERENCE_BUILD_IDENTIFIER;
import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.ReferenceStatus.OK;
import static com.parasoft.findings.jenkins.coverage.model.Metric.*;
import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.Mockito.*;
import static org.xmlunit.assertj.XmlAssert.assertThat;

/**
 * Tests the class {@link CoverageXmlStream}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
class CoverageXmlStreamTest extends SerializableTest<Node> {

    private static final String ACTION_QUALIFIED_NAME = "com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageBuildAction";
    private static final String EMPTY = "[]";

    @Override
    protected Node createSerializable() {
        return new JacocoParser().parse(new InputStreamReader(asInputStream("jacoco-codingstyle.xml"),
                        StandardCharsets.UTF_8), new FilteredLog("Errors"));
    }

    @Test
    void shouldSaveAndRestoreTree() {
        Path saved = createTempFile();
        Node convertedNode = createSerializable();

        var xmlStream = new CoverageXmlStream();
        xmlStream.write(saved, convertedNode);
        Node restored = xmlStream.read(saved);

        Assertions.assertThat(restored).usingRecursiveComparison().isEqualTo(convertedNode);

        var xml = Input.from(saved);
        assertThat(xml).nodesByXPath("//module/values/*")
                .hasSize(4).extractingText()
                .containsExactly("INSTRUCTION: 1260/1350",
                        "BRANCH: 109/116",
                        "LINE: 294/323",
                        "COMPLEXITY: 160");
        assertThat(xml).nodesByXPath("//module/values/*")
                .hasSize(4).extractingText()
                .containsExactly("INSTRUCTION: 1260/1350",
                        "BRANCH: 109/116",
                        "LINE: 294/323",
                        "COMPLEXITY: 160");
        assertThat(xml).nodesByXPath("//file[./name = 'TreeStringBuilder.java']/values/*")
                .hasSize(4).extractingText()
                .containsExactly("INSTRUCTION: 229/233", "BRANCH: 17/18", "LINE: 51/53", "COMPLEXITY: 23");
        assertThat(xml).nodesByXPath("//file[./name = 'TreeStringBuilder.java']/coveredPerLine")
                .hasSize(1).extractingText()
                .containsExactly(
                        "[19: 1, 20: 1, 31: 1, 43: 1, 50: 1, 51: 1, 54: 1, 57: 1, 61: 0, 62: 0, 70: 1, 72: 1, 73: 1, 74: 1, 85: 2, 86: 1, 89: 1, 90: 2, 91: 1, 92: 2, 93: 2, 95: 1, 96: 1, 97: 1, 100: 1, 101: 1, 103: 1, 106: 1, 109: 1, 112: 1, 113: 1, 114: 1, 115: 1, 117: 1, 125: 2, 126: 1, 128: 1, 140: 1, 142: 1, 143: 1, 144: 1, 146: 1, 160: 1, 162: 2, 163: 2, 164: 1, 167: 1, 177: 1, 178: 2, 179: 1, 180: 1, 181: 1, 184: 1]");
        assertThat(xml).nodesByXPath("//file[./name = 'TreeStringBuilder.java']/missedPerLine")
                .hasSize(1).extractingText()
                .containsExactly(
                        "[19: 0, 20: 0, 31: 0, 43: 0, 50: 0, 51: 0, 54: 0, 57: 0, 61: 1, 62: 1, 70: 0, 72: 0, 73: 0, 74: 0, 85: 0, 86: 0, 89: 0, 90: 0, 91: 0, 92: 0, 93: 0, 95: 0, 96: 0, 97: 0, 100: 0, 101: 0, 103: 0, 106: 0, 109: 0, 112: 0, 113: 1, 114: 0, 115: 0, 117: 0, 125: 0, 126: 0, 128: 0, 140: 0, 142: 0, 143: 0, 144: 0, 146: 0, 160: 0, 162: 0, 163: 0, 164: 0, 167: 0, 177: 0, 178: 0, 179: 0, 180: 0, 181: 0, 184: 0]");
    }

    @Test
    void shouldStoreActionCompactly() throws IOException {
        Path saved = createTempFile();
        var xmlStream = new TestXmlStream();
        xmlStream.read(saved); // create the stream

        var file = new XmlFile(xmlStream.getStream(), saved.toFile());
        file.write(createAction());

        assertThat(Input.from(saved)).nodesByXPath("//" + ACTION_QUALIFIED_NAME + "/projectValues/*")
                .hasSize(12).extractingText()
                .containsExactly("MODULE: 1/1",
                        "PACKAGE: 1/1",
                        "FILE: 7/10",
                        "CLASS: 15/18",
                        "METHOD: 97/102",
                        "LINE: 294/323",
                        "BRANCH: 109/116",
                        "INSTRUCTION: 1260/1350",
                        "COMPLEXITY: 160",
                        "COMPLEXITY_MAXIMUM: 6",
                        "COMPLEXITY_DENSITY: 160/323",
                        "LOC: 323");

        assertThat(Input.from(saved)).nodesByXPath("//" + ACTION_QUALIFIED_NAME + "/projectValues/com.parasoft.findings.jenkins.coverage.model.Coverage")
                .hasSize(8).extractingText()
                .containsExactly("MODULE: 1/1",
                        "PACKAGE: 1/1",
                        "FILE: 7/10",
                        "CLASS: 15/18",
                        "METHOD: 97/102",
                        "LINE: 294/323",
                        "BRANCH: 109/116",
                        "INSTRUCTION: 1260/1350");

        var action = file.read();
        assertThat(action).isNotNull().isInstanceOfSatisfying(CoverageBuildAction.class, a ->
                Assertions.assertThat(serializeValues(a))
                        .containsExactly("MODULE: 1/1", "PACKAGE: 1/1", "FILE: 7/10", "CLASS: 15/18",
                                "METHOD: 97/102",
                                "LINE: 294/323", "BRANCH: 109/116", "INSTRUCTION: 1260/1350",
                                "COMPLEXITY: 160", "COMPLEXITY_MAXIMUM: 6", "COMPLEXITY_DENSITY: 160/323",
                                "LOC: 323"
                        ));
    }

    private static List<String> serializeValues(final CoverageBuildAction a) {
        return a.getAllValues(Baseline.PROJECT).stream()
                .map(Value::serialize)
                .collect(Collectors.toList());
    }

    @Test
    void shouldConvertMetricMap2String() {
        NavigableMap<Metric, Fraction> map = new TreeMap<>();

        MetricFractionMapConverter converter = new MetricFractionMapConverter();

        assertThat(converter.marshal(map)).isEqualTo(EMPTY);

        map.put(BRANCH, Fraction.getFraction(50, 100));
        assertThat(converter.marshal(map)).isEqualTo("[BRANCH: 50/100]");

        map.put(LINE, Fraction.getFraction(3, 4));
        assertThat(converter.marshal(map)).isEqualTo("[LINE: 3/4, BRANCH: 50/100]");
    }

    @Test
    void shouldConvertString2MetricMap() {
        MetricFractionMapConverter converter = new MetricFractionMapConverter();

        Assertions.assertThat(converter.unmarshal(EMPTY)).isEmpty();
        Fraction first = Fraction.getFraction(50, 100);
        Assertions.assertThat(converter.unmarshal("[BRANCH: 50/100]"))
                .containsExactly(entry(BRANCH, first));
        Assertions.assertThat(converter.unmarshal("[LINE: 3/4, BRANCH: 50/100]"))
                .containsExactly(entry(LINE, Fraction.getFraction(3, 4)),
                        entry(BRANCH, first));
    }

    @Test
    void shouldConvertIntegerMap2String() {
        NavigableMap<Integer, Integer> map = new TreeMap<>();

        IntegerLineMapConverter converter = new IntegerLineMapConverter();

        assertThat(converter.marshal(map)).isEqualTo(EMPTY);

        map.put(10, 20);
        assertThat(converter.marshal(map)).isEqualTo("[10: 20]");

        map.put(15, 25);
        assertThat(converter.marshal(map)).isEqualTo("[10: 20, 15: 25]");
    }

    @Test
    void shouldConvertString2IntegerMap() {
        IntegerLineMapConverter converter = new IntegerLineMapConverter();

        Assertions.assertThat(converter.unmarshal(EMPTY)).isEmpty();
        Assertions.assertThat(converter.unmarshal("[15: 25]")).containsExactly(entry(15, 25));
        Assertions.assertThat(converter.unmarshal("[15:25, 10: 20]")).containsExactly(entry(10, 20), entry(15, 25));
    }

    @Test
    void shouldConvertIntegerSet2String() {
        NavigableSet<Integer> set = new TreeSet<>();

        IntegerSetConverter converter = new IntegerSetConverter();

        assertThat(converter.marshal(set)).isEqualTo(EMPTY);

        set.add(10);
        assertThat(converter.marshal(set)).isEqualTo("[10]");

        set.add(15);
        assertThat(converter.marshal(set)).isEqualTo("[10, 15]");
    }

    @Test
    void shouldConvertString2IntegerSet() {
        IntegerSetConverter converter = new IntegerSetConverter();

        Assertions.assertThat(converter.unmarshal(EMPTY)).isEmpty();
        Assertions.assertThat(converter.unmarshal("[15]")).containsExactly(15);
        Assertions.assertThat(converter.unmarshal("[15, 20]")).containsExactly(15, 20);
    }

    // TODO: Add content for the other baselines as well
    CoverageBuildAction createAction() {
        var tree = createSerializable();

        return new CoverageBuildAction(mock(FreeStyleBuild.class), ParasoftCoverageRecorder.PARASOFT_COVERAGE_ID,
                StringUtils.EMPTY,
                tree, new QualityGateResult(), new FilteredLog("Test"), "-",
                List.of(), false, new ReferenceResult(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER));
    }

    private static class TestXmlStream extends CoverageXmlStream {
        private XStream2 xStream;

        @Override
        protected void configureXStream(final XStream2 xStream2) {
            super.configureXStream(xStream2);

            this.xStream = xStream2;
        }

        public XStream2 getStream() {
            return xStream;
        }
    }
}
