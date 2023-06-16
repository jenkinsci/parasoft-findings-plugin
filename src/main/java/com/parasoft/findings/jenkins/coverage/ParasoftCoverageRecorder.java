/*
 * Copyright 2023 Parasoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parasoft.findings.jenkins.coverage;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import io.jenkins.plugins.coverage.metrics.steps.CoverageRecorder;
import io.jenkins.plugins.coverage.metrics.steps.CoverageTool;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.List;

public class ParasoftCoverageRecorder extends Recorder {

    private String pattern = StringUtils.EMPTY;
    private static final String PARASOFT_COVERAGE_ID = "parasoft-coverage";
    private static final String PARASOFT_COVERAGE_NAME = "Parasoft Coverage";

    @DataBoundConstructor
    public ParasoftCoverageRecorder() {
        super();
        // empty constructor required for Stapler
    }

    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        CoverageRecorder recorder = setUpCoverageRecorder(pattern);
        return recorder.perform(build, launcher, listener);
    }

    @Override
    public Descriptor getDescriptor() {
        return (Descriptor) super.getDescriptor();
    }

    private static CoverageRecorder setUpCoverageRecorder(final String pattern) {
        CoverageRecorder recorder = new CoverageRecorder();
        CoverageTool tool = new CoverageTool();
        tool.setParser(CoverageTool.Parser.COBERTURA);
        tool.setPattern(pattern);
        recorder.setTools(List.of(tool));
        recorder.setId(PARASOFT_COVERAGE_ID);
        recorder.setName(PARASOFT_COVERAGE_NAME);
        return recorder;
    }

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Recorder_Name();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
