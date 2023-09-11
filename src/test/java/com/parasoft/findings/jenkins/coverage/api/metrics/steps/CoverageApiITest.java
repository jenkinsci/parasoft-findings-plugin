package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageITest;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTool.Parser;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;

import static com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest.JACOCO_ANALYSIS_MODEL_FILE;
import static com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest.JACOCO_CODING_STYLE_FILE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/**
 * Tests the class {@link CoverageApi}.
 *
 * @author Ullrich Hafner
 */
class CoverageApiITest extends AbstractCoverageITest {
    @Test
    void shouldProvideRemoteApi() {
        FreeStyleProject project = createFreestyleJob(Parser.JACOCO, JACOCO_ANALYSIS_MODEL_FILE);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        var remoteApiResult = callRemoteApi(build);
        assertThatJson(remoteApiResult)
                .node("projectStatistics").isEqualTo("{\n"
                        + "  \"branch\": \"88.28%\",\n"
                        + "  \"complexity\": \"2558\",\n"
                        + "  \"complexity-density\": \"+44.12%\",\n"
                        + "  \"complexity-maximum\": \"21\",\n"
                        + "  \"file\": \"99.67%\",\n"
                        + "  \"instruction\": \"96.11%\",\n"
                        + "  \"line\": \"95.39%\",\n"
                        + "  \"loc\": \"5798\",\n"
                        + "  \"method\": \"97.29%\",\n"
                        + "  \"module\": \"100.00%\",\n"
                        + "  \"package\": \"100.00%\"}");
        assertThatJson(remoteApiResult)
                .node("modifiedFilesStatistics").isEqualTo("{}");
        assertThatJson(remoteApiResult)
                .node("modifiedLinesStatistics").isEqualTo("{}");
    }

    @Test
    void shouldShowDeltaInRemoteApi() {
        FreeStyleProject project = createFreestyleJob(Parser.JACOCO,
                JACOCO_ANALYSIS_MODEL_FILE, JACOCO_CODING_STYLE_FILE);

        buildSuccessfully(project);
        // update parser pattern to pick only the coding style results
        project.getPublishersList().get(CoverageRecorder.class).getTools().get(0).setPattern(JACOCO_CODING_STYLE_FILE);
        Run<?, ?> secondBuild = buildSuccessfully(project);

        var remoteApiResult = callRemoteApi(secondBuild);
        assertThatJson(remoteApiResult)
                .node("projectDelta").isEqualTo("{\n"
                        + "  \"branch\": \"+5.33%\",\n"
                        + "  \"complexity\": \"-2558\",\n"
                        + "  \"complexity-density\": \"+5.13%\",\n"
                        + "  \"complexity-maximum\": \"-15\",\n"
                        + "  \"file\": \"-28.74%\",\n"
                        + "  \"instruction\": \"-2.63%\",\n"
                        + "  \"line\": \"-4.14%\",\n"
                        + "  \"loc\": \"-5798\",\n"
                        + "  \"method\": \"-2.06%\",\n"
                        + "  \"module\": \"+0.00%\",\n"
                        + "  \"package\": \"+0.00%\"\n"
                        + "}");
        assertThatJson(remoteApiResult).node("referenceBuild").asString()
                .matches("<a href=\".*jenkins/job/test0/1/\".*>test0 #1</a>");
    }

    private JSONObject callRemoteApi(final Run<?, ?> build) {
        return callJsonRemoteApi(build.getUrl() + "coverage/api/json").getJSONObject();
    }
}
