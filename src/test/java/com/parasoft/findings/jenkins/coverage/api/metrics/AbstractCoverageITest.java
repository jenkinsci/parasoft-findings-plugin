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

package com.parasoft.findings.jenkins.coverage.api.metrics;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;

import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTool.Parser;
import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerSuite;

/**
 * Provides some helper methods to create different job types that will record code coverage results.
 *
 * @author Ullrich Hafner
 */
public abstract class AbstractCoverageITest extends IntegrationTestWithJenkinsPerSuite {
    protected FreeStyleProject createFreestyleJob(final Parser parser, final String... fileNames) {
        return createFreestyleJob(parser, i -> { }, fileNames);
    }

    protected FreeStyleProject createFreestyleJob(final Parser parser,
                                                  final Consumer<ParasoftCoverageRecorder> configuration, final String... fileNames) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(fileNames);

        addCoverageRecorder(project, parser, "**/*xml", configuration);

        return project;
    }

    protected void addCoverageRecorder(final FreeStyleProject project,
            final Parser parser, final String pattern) {
        addCoverageRecorder(project, parser, pattern, i -> { });
    }

    void addCoverageRecorder(final FreeStyleProject project,
            final Parser parser, final String pattern, final Consumer<ParasoftCoverageRecorder> configuration) {
        ParasoftCoverageRecorder recorder = new ParasoftCoverageRecorder();

        configuration.accept(recorder);

        try {
            project.getPublishersList().remove(ParasoftCoverageRecorder.class);
        }
        catch (IOException exception) {
            // ignore and continue
        }
        project.getPublishersList().add(recorder);
    }

    protected WorkflowJob createPipeline(final Parser parser, final String... fileNames) {
        WorkflowJob job = createPipelineWithWorkspaceFiles(fileNames);

        setPipelineScript(job,
                "recordCoverage tools: [[parser: '" + parser.name() + "', pattern: '**/*xml']]");

        return job;
    }

    protected void setPipelineScript(final WorkflowJob job, final String recorderSnippet) {
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + recorderSnippet + "\n"
                        + " }\n", true));
    }

    protected WorkflowJob createDeclarativePipeline(final Parser parser, final String... fileNames) {
        WorkflowJob job = createPipelineWithWorkspaceFiles(fileNames);

        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "    agent any\n"
                + "    stages {\n"
                + "        stage('Test') {\n"
                + "            steps {\n"
                + "                    recordCoverage(\n"
                + "                        tools: [[parser: '" + parser.name() + "', pattern: '**/*xml']]"
                + "            )}\n"
                + "        }\n"
                + "    }\n"
                + "}", true));
        return job;
    }
}
