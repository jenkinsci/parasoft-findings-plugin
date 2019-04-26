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
import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.reports.jenkins.parser.DupIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.FlowIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
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
    private String _workspace;

    @DataBoundConstructor
    public ParasoftTool()
    {
        super();
    }

    @Override
    public IssueParser createParser()
    {
        return new ParasoftParser(null, _workspace);
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding, final LogHandler logger)
    {
        _workspace = workspace.getRemote();
        return super.scan(run, workspace, sourceCodeEncoding, logger);
    }

    @DataBoundSetter
    public void setSettingsFile(final String settingsFile)
    {
        // TODO - set settings file for parser
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
            return new ParasoftTableModel(getAgeBuilder(build, url), getFileNameRenderer(build), this);
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

        public ParasoftTableModel(AgeBuilder ageBuilder, FileNameRenderer fileNameRenderer, DescriptionProvider descriptionProvider)
        {
            super(ageBuilder, fileNameRenderer, descriptionProvider);
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
            if (properties instanceof FlowIssueAdditionalProperties) {
                return super.formatDetails(issue, description + IHtmlTags.BREAK_LINE_TAG + IHtmlTags.BREAK_LINE_TAG
                    + ((FlowIssueAdditionalProperties) properties).getCallHierarchy(null)); //TODO use getFileNameRenderer() to generate link. as for now link doesn't work
            } else if (properties instanceof DupIssueAdditionalProperties) {
                return super.formatDetails(issue, description + IHtmlTags.BREAK_LINE_TAG + IHtmlTags.BREAK_LINE_TAG
                    + ((DupIssueAdditionalProperties) properties).getCallHierarchy(null));
            }
            return super.formatDetails(issue, description);
        }
    }
}
