package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageITest;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.Baseline;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTool.Parser;
import edu.hm.hafner.coverage.Coverage;
import edu.hm.hafner.coverage.Coverage.CoverageBuilder;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Value;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for different JaCoCo, Cobertura, and PIT files.
 */
class CoveragePluginITest extends AbstractCoverageITest {
    private static final String COBERTURA_HIGHER_COVERAGE_FILE = "cobertura-higher-coverage.xml";
    private static final int COBERTURA_COVERED_LINES = 7;
    private static final int COBERTURA_MISSED_LINES = 0;
    private static final String NO_FILES_FOUND_ERROR_MESSAGE = "[-ERROR-] No files found for pattern '**/*xml'. Configuration error?";

    @Test
    void shouldFailWithoutParserInFreestyleJob() {
        FreeStyleProject project = createFreeStyleProject();

        project.getPublishersList().add(new CoverageRecorder());

        verifyNoParserError(project);
    }

    private void verifyNoParserError(final ParameterizedJob<?, ?> project) {
        Run<?, ?> run = buildWithResult(project, Result.FAILURE);

        assertThat(getConsoleLog(run)).contains("[-ERROR-] No tools defined that will record the coverage files");
    }

    @EnumSource
    @ParameterizedTest(name = "{index} => Freestyle job with parser {0}")
    @DisplayName("Report error but do not fail build in freestyle job when no input files are found")
    void shouldReportErrorWhenNoFilesHaveBeenFoundInFreestyleJob(final Parser parser) {
        FreeStyleProject project = createFreestyleJob(parser);

        verifyLogMessageThatNoFilesFound(project);
    }

    private void verifyLogMessageThatNoFilesFound(final ParameterizedJob<?, ?> project) {
        Run<?, ?> run = buildWithResult(project, Result.SUCCESS);

        assertThat(getConsoleLog(run)).contains(NO_FILES_FOUND_ERROR_MESSAGE,
                "Ignore errors and continue processing");
    }

    @EnumSource
    @ParameterizedTest(name = "{index} => Freestyle job with parser {0}")
    @DisplayName("Report error and fail build in freestyle job when no input files are found")
    void shouldFailBuildWhenNoFilesHaveBeenFoundInFreestyleJob(final Parser parser) {
        FreeStyleProject project = createFreestyleJob(parser, r -> r.setFailOnError(true));

        verifyFailureWhenNoFilesFound(project);
    }

    private void verifyFailureWhenNoFilesFound(final ParameterizedJob<?, ?> project) {
        Run<?, ?> run = buildWithResult(project, Result.FAILURE);

        assertThat(getConsoleLog(run)).contains(NO_FILES_FOUND_ERROR_MESSAGE,
                "Failing build due to some errors during recording of the coverage");
    }

    @Test
    void shouldRecordOneJacocoResultInFreestyleJob() {
        FreeStyleProject project = createFreestyleJob(Parser.JACOCO, JACOCO_ANALYSIS_MODEL_FILE);

        verifyOneJacocoResult(project);
    }

    private void verifyOneJacocoResult(final ParameterizedJob<?, ?> project) {
        Run<?, ?> build = buildSuccessfully(project);

        verifyJaCoCoAction(build.getAction(CoverageBuildAction.class));
    }

    private static void verifyJaCoCoAction(final CoverageBuildAction coverageResult) {
        assertThat(coverageResult.getAllValues(Baseline.PROJECT)).extracting(Value::getMetric)
                .containsExactly(Metric.MODULE,
                        Metric.PACKAGE,
                        Metric.FILE,
                        Metric.CLASS,
                        Metric.METHOD,
                        Metric.LINE,
                        Metric.BRANCH,
                        Metric.INSTRUCTION,
                        Metric.COMPLEXITY,
                        Metric.COMPLEXITY_MAXIMUM,
                        Metric.COMPLEXITY_DENSITY,
                        Metric.LOC);
        assertThat(coverageResult.getMetricsForSummary())
                .containsExactly(Metric.LINE, Metric.BRANCH, Metric.MUTATION, Metric.COMPLEXITY_DENSITY, Metric.LOC);
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .contains(createLineCoverageBuilder()
                        .setCovered(JACOCO_ANALYSIS_MODEL_COVERED)
                        .setMissed(JACOCO_ANALYSIS_MODEL_TOTAL - JACOCO_ANALYSIS_MODEL_COVERED)
                        .build());
    }

    @Test
    void shouldRecordTwoJacocoResultsInFreestyleJob() {
        FreeStyleProject project = createFreestyleJob(Parser.JACOCO,
                JACOCO_ANALYSIS_MODEL_FILE, JACOCO_CODING_STYLE_FILE);
        verifyTwoJacocoResults(project);
    }

    private void verifyTwoJacocoResults(final ParameterizedJob<?, ?> project) {
        Run<?, ?> build = buildSuccessfully(project);

        CoverageBuildAction coverageResult = build.getAction(CoverageBuildAction.class);
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .contains(createLineCoverageBuilder()
                        .setCovered(JACOCO_ANALYSIS_MODEL_COVERED + JACOCO_CODING_STYLE_COVERED)
                        .setMissed(JACOCO_ANALYSIS_MODEL_MISSED + JACOCO_CODING_STYLE_MISSED)
                        .build());
    }

    @Test
    void shouldRecordOneCoberturaResultInFreestyleJob() {
        FreeStyleProject project = createFreestyleJob(Parser.COBERTURA, COBERTURA_HIGHER_COVERAGE_FILE);

        verifyOneCoberturaResult(project);
    }

    private void verifyOneCoberturaResult(final ParameterizedJob<?, ?> project) {
        Run<?, ?> build = buildSuccessfully(project);

        verifyCoberturaAction(build.getAction(CoverageBuildAction.class));
    }

    private static void verifyCoberturaAction(final CoverageBuildAction coverageResult) {
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .contains(new CoverageBuilder().setMetric(Metric.LINE).setCovered(COBERTURA_COVERED_LINES)
                        .setMissed(COBERTURA_MISSED_LINES)
                        .build());
    }

    @Test
    void shouldRecordCoberturaAndJacocoResultsInFreestyleJob() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(JACOCO_ANALYSIS_MODEL_FILE,
                COBERTURA_HIGHER_COVERAGE_FILE);

        CoverageRecorder recorder = new CoverageRecorder();

        var cobertura = new CoverageTool();
        cobertura.setParser(Parser.COBERTURA);
        cobertura.setPattern(COBERTURA_HIGHER_COVERAGE_FILE);

        var jacoco = new CoverageTool();
        jacoco.setParser(Parser.JACOCO);
        jacoco.setPattern(JACOCO_ANALYSIS_MODEL_FILE);

        recorder.setTools(List.of(jacoco, cobertura));
        project.getPublishersList().add(recorder);

        verifyForOneCoberturaAndOneJacoco(project);
    }


    private void verifyForOneCoberturaAndOneJacoco(final ParameterizedJob<?, ?> project) {
        Run<?, ?> build = buildSuccessfully(project);

        CoverageBuildAction coverageResult = build.getAction(CoverageBuildAction.class);
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .contains(createLineCoverageBuilder()
                        .setCovered(JACOCO_ANALYSIS_MODEL_COVERED + COBERTURA_COVERED_LINES)
                        .setMissed(JACOCO_ANALYSIS_MODEL_MISSED)
                        .build());
    }

    @Test
    void shouldRecordOnePitResultInFreestyleJob() {
        FreeStyleProject project = createFreestyleJob(Parser.PIT, "mutations.xml");

        verifyOnePitResult(project);
    }

    private void verifyOnePitResult(final ParameterizedJob<?, ?> project) {
        Run<?, ?> build = buildSuccessfully(project);

        CoverageBuildAction coverageResult = build.getAction(CoverageBuildAction.class);
        assertThat(coverageResult.getAllValues(Baseline.PROJECT))
                .filteredOn(Value::getMetric, Metric.MUTATION)
                .first()
                .isInstanceOfSatisfying(Coverage.class, m -> {
                    assertThat(m.getCovered()).isEqualTo(222);
                    assertThat(m.getTotal()).isEqualTo(246);
                });
    }

    private static CoverageBuilder createLineCoverageBuilder() {
        return new CoverageBuilder().setMetric(Metric.LINE);
    }
}
