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

package com.parasoft.xtest.reports.jenkins.tool;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.reports.jenkins.internal.rules.JenkinsRulesUtil;
import com.parasoft.xtest.reports.jenkins.internal.rules.RuleDocumentationReader;
import com.parasoft.xtest.reports.jenkins.internal.rules.RuleDocumentationStorage;
import com.parasoft.xtest.reports.jenkins.internal.variables.JenkinsVariablesResolver;
import com.parasoft.xtest.reports.jenkins.parser.DupIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.FlowIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.LogHandler;

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
        this._localSettingsPath = localSettingsPath;
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
        public Descriptor()
        {
            super(PLUGIN_ID);
        }

        @Override
        public String getDisplayName()
        {
            return "Parasoft Findings"; //$NON-NLS-1$
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
    }

    private static class LabelProvider
        extends IconLabelProvider
    {

        private static final String ICONS_PREFIX = "/plugin/parasoft-findings/icons/"; //$NON-NLS-1$

        LabelProvider()
        {
            super(PLUGIN_ID, "Parasoft"); //$NON-NLS-1$
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
        public DetailsTableModel getIssuesModel(Run<?, ?> build, String url)
        {
            return new ParasoftTableModel(build, getAgeBuilder(build, url), getFileNameRenderer(build), this);
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

    private static class ParasoftTableModel
        extends DetailsTableModel
    {
        private RuleDocumentationReader _ruleDocReader = null;

        public ParasoftTableModel(Run<?, ?> build, AgeBuilder ageBuilder, FileNameRenderer fileNameRenderer, DescriptionProvider descriptionProvider)
        {
            super(ageBuilder, fileNameRenderer, descriptionProvider);
            _ruleDocReader = new RuleDocumentationReader(build.getRootDir());
        }

        @Override
        public List<Integer> getWidths(Report report)
        {
            List<Integer> widths = super.getWidths(report);
            widths.add(1);
            widths.add(1);
            return widths;
        }

        @Override
        public List<String> getHeaders(Report report)
        {
            List<String> headers = super.getHeaders(report);
            headers.add(Messages.AUTHOR_COLUMN_HEADER);
            headers.add(Messages.REVISION_COLUMN_HEADER);
            return headers;
        }

        @Override
        protected List<String> getRow(Report report, Issue issue, String description)
        {
            List<String> row = super.getRow(report, issue, description);
            Serializable additionalProperties = issue.getAdditionalProperties();
            if (additionalProperties instanceof ParasoftIssueAdditionalProperties) {
                ParasoftIssueAdditionalProperties parasoftIssueAdditionalProperties = (ParasoftIssueAdditionalProperties) additionalProperties;
                row.add(parasoftIssueAdditionalProperties.getAuthor());
                row.add(parasoftIssueAdditionalProperties.getRevision());
            } else {
                row.add("-"); //$NON-NLS-1$
                row.add("-"); //$NON-NLS-1$
            }
            return row;
        }

        @Override
        protected String formatDetails(Issue issue, String description)
        {
            Serializable properties = issue.getAdditionalProperties();
            if (!(properties instanceof ParasoftIssueAdditionalProperties)) {
                return super.formatDetails(issue, description);
            }
            StringBuilder sb = new StringBuilder();
            
            if (properties instanceof FlowIssueAdditionalProperties) {
                sb.append(IHtmlTags.BREAK_LINE_TAG + ((FlowIssueAdditionalProperties) properties).getCallHierarchy(null));
            } else if (properties instanceof DupIssueAdditionalProperties) {
                sb.append(IHtmlTags.BREAK_LINE_TAG + ((DupIssueAdditionalProperties) properties).getCallHierarchy(null));
            }
            String analyzer = ((ParasoftIssueAdditionalProperties)properties).getAnalyzer();
            String ruleId = issue.getType();
            String ruleDocContents = _ruleDocReader.getRuleDoc(analyzer, ruleId);

            if (UString.isNonEmpty(ruleDocContents)) {
                sb.append(IHtmlTags.BREAK_LINE_TAG + IHtmlTags.PARAGRAPH_START_TAG + ruleDocContents + IHtmlTags.PARAGRAPH_END_TAG);
            } else if (UString.isNonEmptyTrimmed(ruleId)) {
                //sb.append(IHtmlTags.BREAK_LINE_TAG + NLS.getFormatted(Messages.RULE_DOCUMENTATION_UNAVAILABLE, ruleId));
            }
            return super.formatDetails(issue, sb.toString());
        }
    }
}
