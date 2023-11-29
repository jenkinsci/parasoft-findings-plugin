/*
 * Copyright 2019 Parasoft Corporation
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

package com.parasoft.findings.jenkins.tool;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.parasoft.findings.jenkins.parser.DupIssueAdditionalProperties;
import com.parasoft.findings.jenkins.parser.FlowIssueAdditionalProperties;
import com.parasoft.findings.jenkins.parser.ParasoftIssueAdditionalProperties;
import com.parasoft.findings.jenkins.parser.ParasoftParser;
import edu.hm.hafner.util.VisibleForTesting;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.parasoft.findings.jenkins.html.IHtmlTags;
import com.parasoft.findings.jenkins.internal.rules.JenkinsRulesUtil;
import com.parasoft.findings.jenkins.internal.rules.RuleDocumentationStorage;
import com.parasoft.findings.jenkins.internal.services.JenkinsServicesProvider;
import com.parasoft.findings.jenkins.internal.variables.JenkinsVariablesResolver;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.JenkinsFacade;

public class ParasoftTool
    extends ReportScanningTool
{
    private static final long serialVersionUID = -5773171179445359278L;
    private final static String PLUGIN_ID = "parasoft-findings"; //$NON-NLS-1$
    private String _localSettingsPath = StringUtils.EMPTY;

    private String _workspace = null;
    private Properties _settings = null;

    @DataBoundConstructor
    public ParasoftTool()
    {
        super();
    }

    @Override
    public IssueParser createParser()
    {
        return new ParasoftParser(_settings, _workspace);
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding, final LogHandler logger)
    {
        _workspace = workspace.getRemote();

        String resolvedSettingsPath = null;
        try {
            Map<String, String> envVars = run.getEnvironment(TaskListener.NULL);
            JenkinsVariablesResolver variablesResolver = new JenkinsVariablesResolver(envVars);
            resolvedSettingsPath = variablesResolver.performSubstitution(getLocalSettingsPath());
        } catch (Exception e) {
            Logger.getLogger().warn(e);
        }
        _settings = JenkinsRulesUtil.loadSettings(workspace, resolvedSettingsPath);

        Report report = super.scan(run, workspace, sourceCodeEncoding, logger);

        Iterator<Issue> issues = report.iterator();

        JenkinsServicesProvider.init();
        RuleDocumentationStorage storage = new RuleDocumentationStorage(run.getRootDir(), _settings);
        while (issues.hasNext()) {

            Issue issue = issues.next();
            Serializable additionalProperties = issue.getAdditionalProperties();
            if (!(additionalProperties instanceof ParasoftIssueAdditionalProperties)) {
                continue;
            }
            String ruleId = issue.getType();
            String analyzer = ((ParasoftIssueAdditionalProperties)additionalProperties).getAnalyzer();

            storage.storeRuleDoc(analyzer, ruleId);
        }
        return report;
    }

    @DataBoundSetter
    public void setLocalSettingsPath(final String localSettingsPath)
    {
        _localSettingsPath = localSettingsPath;
    }

    @Nullable
    public String getLocalSettingsPath()
    {
        return _localSettingsPath;
    }

    @Extension
    @Symbol("parasoftFindings")
    public static class Descriptor
        extends ReportScanningToolDescriptor
    {
        // Maintain backward compatibility.
        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias("com.parasoft.xtest.reports.jenkins.tool.ParasoftTool",
                    ParasoftTool.class);
        }

        public Descriptor()
        {
            super(PLUGIN_ID);
        }

        @Override
        public String getDisplayName()
        {
            return Messages.PARASOFT_TOOL_DISPLAY_NAME();
        }

        @Override
        public boolean canScanConsoleLog()
        {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider()
        {
            return new LabelProvider();
        }

        @Override
        public String getPattern() {
            return "**/report.xml"; //$NON-NLS-1$
        }

        @Override
        public String getUrl() {
            return "https://www.parasoft.com/"; //$NON-NLS-1$
        }
    }

   public static class LabelProvider
        extends IconLabelProvider
    {

        private static final String ICONS_PREFIX = "/plugin/parasoft-findings/icons/"; //$NON-NLS-1$

        private JenkinsFacade jenkins = new JenkinsFacade();

        @VisibleForTesting
        public void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
            this.jenkins = jenkinsFacade;
        }

        LabelProvider()
        {
            super(PLUGIN_ID, Messages.PARASOFT_NAME());
        }

        @Override
        public String getLargeIconUrl()
        {
            return ICONS_PREFIX + "parasofttest48.png"; //$NON-NLS-1$
        }

        @Override
        public String getSmallIconUrl()
        {
            return ICONS_PREFIX + "parasofttest24.png"; //$NON-NLS-1$
        }

        @Override
        public DetailsTableModel getIssuesModel(Run<?, ?> build, String url, Report report)
        {
            return new ParasoftTableModel(build, report, getFileNameRenderer(build), getAgeBuilder(build, url), this, jenkins);
        }

        @Override
        public String getSourceCodeDescription(Run<?, ?> build, Issue issue)
        {
            String description = super.getSourceCodeDescription(build, issue);
            Serializable properties = issue.getAdditionalProperties();
            if (properties instanceof FlowIssueAdditionalProperties) {
                description += IHtmlTags.BREAK_LINE_TAG + ((FlowIssueAdditionalProperties) properties).getCallHierarchy(null);
            } else if (properties instanceof DupIssueAdditionalProperties) {
                description += IHtmlTags.BREAK_LINE_TAG + ((DupIssueAdditionalProperties) properties).getCallHierarchy(null);
            }
            return description;
        }
    }
}
