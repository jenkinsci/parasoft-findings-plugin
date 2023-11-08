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
import java.util.TreeMap;

import com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder;
import io.jenkins.plugins.forensics.reference.ReferenceBuild;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import com.parasoft.findings.jenkins.coverage.model.Coverage.CoverageBuilder;
import com.parasoft.findings.jenkins.coverage.model.Metric;
import com.parasoft.findings.jenkins.coverage.model.ModuleNode;
import com.parasoft.findings.jenkins.coverage.model.Node;
import edu.hm.hafner.util.FilteredLog;

import hudson.model.FreeStyleBuild;

import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import io.jenkins.plugins.util.QualityGateResult;

import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.DEFAULT_REFERENCE_BUILD_IDENTIFIER;
import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.ReferenceResult.ReferenceStatus.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link CoverageBuildAction}.
 *
 * @author Ullrich Hafner
 */
@DefaultLocale("en")
class CoverageBuildActionTest {
    public static final String NO_REFERENCE_BUILD = "-";

    @Test
    void shouldNotLoadResultIfCoverageValuesArePersistedInAction() {
        Node module = new ModuleNode("module");

        var coverageBuilder = new CoverageBuilder();
        var percent50 = coverageBuilder.setMetric(Metric.BRANCH).setCovered(1).setMissed(1).build();
        var percent80 = coverageBuilder.setMetric(Metric.LINE).setCovered(8).setMissed(2).build();

        module.addValue(percent50);
        module.addValue(percent80);

        var deltas = new TreeMap<Metric, Fraction>();
        var lineDelta = percent80.getCoveredPercentage().subtract(percent50.getCoveredPercentage());
        deltas.put(Metric.LINE, lineDelta);
        var branchDelta = percent50.getCoveredPercentage().subtract(percent80.getCoveredPercentage());
        deltas.put(Metric.BRANCH, branchDelta);

        var coverages = List.of(percent50, percent80);
        var action = createAction(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);

        when(action.getResult()).thenThrow(new IllegalStateException("Result should not be accessed with getResult() when getting a coverage metric that is persisted in the build"));


        assertThat(action.getStatistics().getValue(Baseline.PROJECT, Metric.BRANCH)).hasValue(percent50);
        assertThat(action.getStatistics().getValue(Baseline.PROJECT, Metric.LINE)).hasValue(percent80);
        assertThat(action.getStatistics().getValue(Baseline.MODIFIED_LINES, Metric.BRANCH)).hasValue(percent50);
        assertThat(action.getStatistics().getValue(Baseline.MODIFIED_LINES, Metric.LINE)).hasValue(percent80);

        assertThat(action.getAllValues(Baseline.PROJECT)).containsAll(coverages);
    }

    private static CoverageBuildAction createEmptyAction(final Node module) {
        return new CoverageBuildAction(mock(FreeStyleBuild.class), ParasoftCoverageRecorder.PARASOFT_COVERAGE_ID,
                StringUtils.EMPTY, module, new QualityGateResult(), createLog(), NO_REFERENCE_BUILD,
                List.of(), false, new ReferenceResult(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER));
    }

    private static FilteredLog createLog() {
        FilteredLog filteredLog = new FilteredLog("Errors");
        filteredLog.logInfo("This is a log message.");
        return filteredLog;
    }

    @Test
    void shouldCreateViewModel() {
        Node root = new ModuleNode("top-level");
        CoverageBuildAction action = createEmptyAction(root);

        assertThat(action.getTarget()).extracting(CoverageViewModel::getNode).isSameAs(root);
        assertThat(action.getTarget()).extracting(CoverageViewModel::getOwner).isSameAs(action.getOwner());
    }

    @Test
    void shouldGetVariables() {
        var action = createAction(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);

        //Test getLog()
        assertThat(action.getLog().getInfoMessages().toString()).isEqualTo("[This is a log message.]");
        //Test getQualityGateResult()
        assertThat(action.getQualityGateResult().getOverallStatus().toString()).isEqualTo("INACTIVE");
        //Test getFormatter()
        assertThat(action.getFormatter().getDisplayName(Metric.LINE)).isEqualTo("Coverage");
        //Test getProjectBaseline()
        assertThat(action.getProjectBaseline().getTitle()).isEqualTo("Overall project");
        //Test hasBaselineResult()
        assertThat(action.hasBaselineResult(Baseline.PROJECT)).isTrue();
        //Test getTitle()
        assertThat(action.getTitle(Baseline.MODIFIED_LINES)).isEqualTo("Modified code lines");
        //Test getValues()
        assertThat(action.getValues(Baseline.PROJECT).toString()).isEqualTo("[LINE: 80.00% (8/10), LOC: 10]");
        assertThat(action.getValues(Baseline.MODIFIED_LINES).toString()).isEqualTo("[LINE: 80.00% (8/10)]");
        //Test getValueForMetric()
        assertThat(action.getValueForMetric(Baseline.PROJECT, Metric.LINE).toString()).isEqualTo("Optional[LINE: 80.00% (8/10)]");
        //Test toString()
        assertThat(action.toString()).isEqualTo("Parasoft Coverage (parasoft-coverage): [MODULE: 100.00% (1/1), LINE: 80.00% (8/10), BRANCH: 50.00% (1/2), LOC: 10]");
        //Test getIconFileName()
        assertThat(action.getIconFileName()).isEqualTo("");
    }

    @Test
    void testGetReferenceBuildLink() {
        var action = createAction(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);
        //No reference build
        assertThat(action.getReferenceBuildLink()).isEqualTo(NO_REFERENCE_BUILD);

        mockStatic(ReferenceBuild.class);

        //Reference build removed
        when(ReferenceBuild.getReferenceBuildLink(any())).thenReturn("#1");
        action = createAction(OK, "1", "1");
        assertThat(action.getReferenceBuildLink()).isEqualTo("1 (removed)");

        //Valid reference build id
        when(ReferenceBuild.getReferenceBuildLink(any())).thenReturn("Reference build link");
        assertThat(action.getReferenceBuildLink()).isEqualTo("Reference build link");
    }

    @Test
    void testFormatValue() {
        var action = createAction(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);

        assertThat(action.formatValue(Baseline.PROJECT, Metric.LINE)).isEqualTo("80.00% (8/10)");
        assertThat(action.formatValue(Baseline.MODIFIED_LINES, Metric.LOC)).isEqualTo("N/A");
    }

    @Test
    void testGetReferenceBuildWarningMessage() {
        //When referenceStatus is 'NO_REF_JOB'
        var action = createAction(NO_REF_JOB, "not-existing-job", DEFAULT_REFERENCE_BUILD_IDENTIFIER,
                NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage())
                .isEqualTo("The specified reference job 'not-existing-job' could not be found");

        //When referenceStatus is 'NO_REF_BUILD', and referenceBuild is DEFAULT_REFERENCE_BUILD_IDENTIFIER
        action = createAction(NO_REF_BUILD, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("No stable build was found in job 'test_project'");

        //When referenceStatus is 'NO_REF_BUILD', and referenceBuild is not DEFAULT_REFERENCE_BUILD_IDENTIFIER
        action = createAction(NO_REF_BUILD, "1", NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("The specified reference build '1' could not be found in job 'test_project'");

        //When referenceStatus is 'NO_CVG_DATA_IN_REF_BUILD', and referenceBuild is DEFAULT_REFERENCE_BUILD_IDENTIFIER
        action = createAction(NO_CVG_DATA_IN_REF_BUILD, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("No Parasoft code coverage result was found in any of the previous stable builds in job 'test_project'");

        //When referenceStatus is 'NO_CVG_DATA_IN_REF_BUILD', and referenceBuild is not DEFAULT_REFERENCE_BUILD_IDENTIFIER
        action = createAction(NO_CVG_DATA_IN_REF_BUILD, "test_project#1", NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("No Parasoft code coverage result was found in reference build 'test_project#1'");

        //When referenceStatus is 'REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE'
        action = createAction(REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE, "test_project#1", NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("The reference build 'test_project#1' cannot be used. Only stable or unstable builds are valid references");

        //When referenceStatus is 'REF_BUILD_IS_CURRENT_BUILD'
        action = createAction(REF_BUILD_IS_CURRENT_BUILD, "test_project#1", NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("The reference build 'test_project#1' was ignored since the build number set is same as the current build");

        //When referenceStatus is 'NO_PREVIOUS_BUILD_WAS_FOUND'
        action = createAction(NO_PREVIOUS_BUILD_WAS_FOUND, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("No previous build was found in job 'test_project'");

        //When referenceStatus is 'OK'
        action = createAction(OK, DEFAULT_REFERENCE_BUILD_IDENTIFIER, NO_REFERENCE_BUILD);
        assertThat(action.getReferenceBuildWarningMessage()).isEqualTo("");
    }

    private CoverageBuildAction createAction(ReferenceResult.ReferenceStatus status, String referenceBuild,
                                             String referenceBuildId) {
        return createAction(status, "test_project", referenceBuild, referenceBuildId);
    }

    private CoverageBuildAction createAction(ReferenceResult.ReferenceStatus status, String referenceJob,
                                             String referenceBuild, String referenceBuildId) {
        Node module = new ModuleNode("module");
        var coverageBuilder = new CoverageBuilder();
        var percent50 = coverageBuilder.setMetric(Metric.BRANCH).setCovered(1).setMissed(1).build();
        var percent80 = coverageBuilder.setMetric(Metric.LINE).setCovered(8).setMissed(2).build();
        module.addValue(percent50);
        module.addValue(percent80);

        var coverages = List.of(percent50, percent80);

        return spy(new CoverageBuildAction(mock(FreeStyleBuild.class), ParasoftCoverageRecorder.PARASOFT_COVERAGE_ID,
                StringUtils.EMPTY, module, new QualityGateResult(), createLog(), referenceBuildId, coverages,
                false, new ReferenceResult(status, referenceJob, referenceBuild)));
    }
}
