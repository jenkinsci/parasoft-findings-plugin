/*
 * (C) Copyright ParaSoft Corporation 2019. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins.tool;

import static j2html.TagCreator.div;
import static j2html.TagCreator.join;
import static j2html.TagCreator.p;
import static j2html.TagCreator.strong;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.reports.jenkins.internal.rules.RuleDocumentationReader;
import com.parasoft.xtest.reports.jenkins.parser.DupIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.FlowIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftIssueAdditionalProperties;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import j2html.tags.UnescapedText;

public class ParasoftTableModel
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
        List<Integer> widths = new ArrayList<Integer>();
        widths.add(1);
        widths.add(1);
        if (report.hasPackages()) {
            widths.add(2);
        }
        if (report.hasCategories()) {
            widths.add(1);
        }
        if (report.hasTypes()) {
            widths.add(1);
        }
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        return widths;
    }

    @Override
    public List<String> getHeaders(Report report)
    {
        List<String> visibleColumns = new ArrayList<>();
        visibleColumns.add(Messages.DETAILS_COLUMN_HEADER);
        visibleColumns.add(Messages.FILE_COLUMN_HEADER);
        if (report.hasPackages()) {
            visibleColumns.add(Messages.PACKAGE_COLUMN_HEADER);
        }
        if (report.hasCategories()) {
            visibleColumns.add(Messages.CATEGORY_COLUMN_HEADER);
        }
        if (report.hasTypes()) {
            visibleColumns.add(Messages.TYPE_COLUMN_HEADER);
        }
        visibleColumns.add(Messages.SEVERITY_COLUMN_HEADER);
        visibleColumns.add(Messages.AGE_COLUMN_HEADER);
        visibleColumns.add(Messages.AUTHOR_COLUMN_HEADER);
        visibleColumns.add(Messages.REVISION_COLUMN_HEADER);
        return visibleColumns;
    }

    @Override
    protected void configureColumns(ColumnDefinitionBuilder builder, Report report)
    {
        builder.add("description").add("fileName", "string"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (report.hasPackages()) {
            builder.add("packageName"); //$NON-NLS-1$
        }
        if (report.hasCategories()) {
            builder.add("category"); //$NON-NLS-1$
        }
        if (report.hasTypes()) {
            builder.add("type"); //$NON-NLS-1$
        }
        builder.add("severity").add("age").add("author").add("revision"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public TableRow getRow(Report report, Issue issue)
    {
        return new ParasoftTableRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(), issue);
    }

    public class ParasoftTableRow
        extends TableRow
    {
        private String description;
        private String packageName;
        private String category;
        private String type;
        private String severity;
        private String author = "-"; //$NON-NLS-1$
        private String revision = "-"; //$NON-NLS-1$

        protected ParasoftTableRow(AgeBuilder ageBuilder, FileNameRenderer fileNameRenderer, DescriptionProvider descriptionProvider, Issue issue)
        {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue);
            description = formatDetails(issue, descriptionProvider.getDescription(issue));
            packageName = formatProperty("packageName", issue.getPackageName()); //$NON-NLS-1$
            category = formatProperty("category", issue.getCategory()); //$NON-NLS-1$
            type = formatProperty("type", issue.getType()); //$NON-NLS-1$
            severity = formatSeverity(issue.getSeverity());

            Serializable additionalProperties = issue.getAdditionalProperties();
            if (additionalProperties instanceof ParasoftIssueAdditionalProperties) {
                ParasoftIssueAdditionalProperties parasoftIssueAdditionalProperties = (ParasoftIssueAdditionalProperties) additionalProperties;
                author = formatProperty("author", parasoftIssueAdditionalProperties.getAuthor()); //$NON-NLS-1$
                revision = formatProperty("revision", parasoftIssueAdditionalProperties.getRevision()); //$NON-NLS-1$
            }
        }

        @Override
        public String getDescription()
        {
            return description;
        }

        public String getPackageName()
        {
            return packageName;
        }

        public String getCategory()
        {
            return category;
        }

        public String getType()
        {
            return type;
        }

        public String getSeverity()
        {
            return severity;
        }

        public String getAuthor()
        {
            return author;
        }

        public String getRevision()
        {
            return revision;
        }

        private String formatDetails(Issue issue, String description)
        {
            Serializable properties = issue.getAdditionalProperties();
            if (!(properties instanceof ParasoftIssueAdditionalProperties)) {
                return formatDefaultDetails(issue, description);
            }
            StringBuilder sb = new StringBuilder();

            if (properties instanceof FlowIssueAdditionalProperties) {
                sb.append(IHtmlTags.BREAK_LINE_TAG + ((FlowIssueAdditionalProperties) properties).getCallHierarchy(null));
            } else if (properties instanceof DupIssueAdditionalProperties) {
                sb.append(IHtmlTags.BREAK_LINE_TAG + ((DupIssueAdditionalProperties) properties).getCallHierarchy(null));
            }
            String analyzer = ((ParasoftIssueAdditionalProperties) properties).getAnalyzer();
            String ruleId = issue.getType();
            String ruleDocContents = _ruleDocReader.getRuleDoc(analyzer, ruleId);

            if (UString.isNonEmpty(ruleDocContents)) {
                sb.append(IHtmlTags.BREAK_LINE_TAG + IHtmlTags.PARAGRAPH_START_TAG + ruleDocContents + IHtmlTags.PARAGRAPH_END_TAG);
            } else if (UString.isNonEmptyTrimmed(ruleId)) {
                //sb.append(IHtmlTags.BREAK_LINE_TAG + NLS.getFormatted(Messages.RULE_DOCUMENTATION_UNAVAILABLE, ruleId));
            }
            return formatDefaultDetails(issue, sb.toString());
        }

        private String formatDefaultDetails(final Issue issue, final String additionalDescription)
        {
            UnescapedText details;
            if (StringUtils.isBlank(issue.getMessage())) {
                details = new UnescapedText(additionalDescription);
            } else {
                details = join(p(strong().with(new UnescapedText(issue.getMessage()))), additionalDescription);
            }
            return div().withClass("details-control").attr("data-description", render(details)).render(); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}