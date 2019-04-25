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

package com.parasoft.xtest.reports.jenkins.parser;

import java.io.Serializable;
import java.util.List;

import com.parasoft.xtest.common.collections.UCollection;
import com.parasoft.xtest.reports.jenkins.html.Colors;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;

public class FlowIssueAdditionalProperties
    extends ParasoftIssueAdditionalProperties
{
    private static final long serialVersionUID = 3241507213664883643L;

    private String _parentKey;
    private List<Issue> _children;
    private String _description;
    private String _cause = null;
    private String _point = null;

    public FlowIssueAdditionalProperties()
    {}

    public FlowIssueAdditionalProperties(String author, String revision, String analyzer)
    {
        super(author, revision, analyzer);
    }

    public String getParentKey()
    {
        return _parentKey;
    }
    public List<Issue> getChildren()
    {
        return _children;
    }
    public String getDescription()
    {
        return _description;
    }
    public String getCause()
    {
        return _cause;
    }
    public String getPoint()
    {
        return _point;
    }

    public void setParentKey(String parentKey)
    {
        _parentKey = parentKey;
    }
    public void setChildren(List<Issue> children)
    {
        _children = children;
    }
    public void setDescription(String description)
    {
        _description = description;
    }
    public void setCause(String cause)
    {
        _cause = cause;
    }
    public void setPoint(String point)
    {
        _point = point;
    }

    public String getCallHierarchy(FileNameRenderer fileNameRenderer)
    {
        StringBuilder message = new StringBuilder();
        message.append("<ul>"); //$NON-NLS-1$
        for (Issue child : _children) {
            message.append(getChildrenLinks(child, fileNameRenderer));
        }
        message.append("</ul>"); //$NON-NLS-1$
        return message.toString();
    }

    private String getChildrenLinks(Issue issue, FileNameRenderer fileNameRenderer)
    {
        issue.setFileName(issue.getFileName());
        StringBuilder message = new StringBuilder();
        FlowIssueAdditionalProperties additionalProperties = getAdditionalProperties(issue);
        message.append(IHtmlTags.LIST_ELEM_START_TAG);
        message.append(IHtmlTags.BOLD_START_TAG);
        addCause(message, additionalProperties);
        addPoint(message, additionalProperties);
        message.append(IHtmlTags.BOLD_END_TAG);
        message.append(getLinkToCallPlace(issue, additionalProperties, fileNameRenderer));
        message.append(IHtmlTags.NON_BREAKABLE_SPACE);
        message.append(additionalProperties.getDescription());
        addChildren(message, additionalProperties, fileNameRenderer);
        message.append(IHtmlTags.LIST_ELEM_END_TAG);
        return message.toString();
    }

    private void addCause(StringBuilder message, FlowIssueAdditionalProperties additionalProperties)
    {
        if (additionalProperties.getCause() != null) {
            message.append(Colors.createColorSpanStartTag(Colors.BLUE));
            message.append(additionalProperties.getCause());
            message.append(IHtmlTags.SPAN_END_TAG);
            message.append(IHtmlTags.BREAK_LINE_TAG);
        }
    }

    private void addPoint(StringBuilder message, FlowIssueAdditionalProperties additionalProperties)
    {
        if (additionalProperties.getPoint() != null) {
            message.append(Colors.createColorSpanStartTag(Colors.RED));
            message.append(additionalProperties.getPoint());
            message.append(IHtmlTags.SPAN_END_TAG);
            message.append(IHtmlTags.BREAK_LINE_TAG);
        }
    }

    private void addChildren(StringBuilder message, FlowIssueAdditionalProperties additionalProperties, FileNameRenderer fileNameRenderer)
    {
        List<Issue> children = additionalProperties.getChildren();
        if (UCollection.isNonEmpty(children)) {
            for (Issue child : children) {
                message.append(IHtmlTags.LIST_START_TAG);
                message.append(getChildrenLinks(child, fileNameRenderer));
                message.append(IHtmlTags.LIST_END_TAG);
            }
        }
    }

    private String getLinkToCallPlace(Issue issue, FlowIssueAdditionalProperties additionalProperties, FileNameRenderer fileNameRenderer)
    {
        if (fileNameRenderer != null) {
            return fileNameRenderer.createAffectedFileLink(issue).render();
        } else {
            String message = Colors.createColorSpanStartTag(Colors.GRAY);
            if (additionalProperties.getCause() != null) {
                message = Colors.createColorSpanStartTag(Colors.BLUE);
            } else if (additionalProperties.getPoint() != null) {
                message = Colors.createColorSpanStartTag(Colors.RED);
            }
            message += String.format("%s:%d", issue.getBaseName(), issue.getLineStart()); //$NON-NLS-1$
            message += IHtmlTags.SPAN_END_TAG;
            return message;
        }
    }

    private FlowIssueAdditionalProperties getAdditionalProperties(Issue issue)
    {
        Serializable properties = issue.getAdditionalProperties();
        if (properties instanceof FlowIssueAdditionalProperties) {
            return (FlowIssueAdditionalProperties) properties;
        }
        return null;
    }
}
