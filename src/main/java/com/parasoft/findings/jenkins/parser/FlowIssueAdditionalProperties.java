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

package com.parasoft.findings.jenkins.parser;

import java.io.Serializable;
import java.util.List;

import com.parasoft.findings.utils.common.util.CollectionUtil;
import com.parasoft.findings.jenkins.html.Colors;
import com.parasoft.findings.jenkins.html.IHtmlTags;

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;

public class FlowIssueAdditionalProperties
        extends ParasoftIssueAdditionalProperties
{
    private static final long serialVersionUID = 3241507213664883643L;

    public FlowIssueAdditionalProperties()
    {}

    public FlowIssueAdditionalProperties(String author, String revision, String analyzer)
    {
        super(author, revision, analyzer);
    }

    @SuppressWarnings("unchecked")
    public List<Issue> getChildren()
    {
        return (List<Issue>)get(CHILDREN_KEY);
    }

    public String getDescription()
    {
        return (String)get(DESCRIPTION_KEY);
    }

    public String getCause()
    {
        return (String)get(CAUSE_KEY);
    }

    public String getPoint()
    {
        return (String)get(POINT_KEY);
    }

    public void setParentKey(String parentKey)
    {
        put(PARENT_KEY, parentKey);
    }

    public void setChildren(List<Issue> children)
    {
        put(CHILDREN_KEY, children);
    }

    public void setDescription(String description)
    {
        put(DESCRIPTION_KEY, description);
    }

    public void setCause(String cause)
    {
        put(CAUSE_KEY, cause);
    }

    public void setPoint(String point)
    {
        put(POINT_KEY, point);
    }

    public String getCallHierarchy(FileNameRenderer fileNameRenderer)
    {
        StringBuilder message = new StringBuilder();
        message.append("<ul>"); //$NON-NLS-1$
        for (Issue child : getChildren()) {
            String childDesc = getChildDescription(child, fileNameRenderer);
            if (childDesc != null) {
                message.append(childDesc);
            }
        }
        message.append("</ul>"); //$NON-NLS-1$
        return message.toString();
    }

    private String getChildDescription(Issue issue, FileNameRenderer fileNameRenderer)
    {
        FlowIssueAdditionalProperties additionalProperties = getAdditionalProperties(issue);
        if (additionalProperties == null) {
            return null;
        }

        StringBuilder message = new StringBuilder();
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
        if (CollectionUtil.isNonEmpty(children)) {
            for (Issue child : children) {
                message.append(IHtmlTags.LIST_START_TAG);
                message.append(getChildDescription(child, fileNameRenderer));
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

    public static final String CAUSE_KEY = "cause"; //$NON-NLS-1$
    public static final String POINT_KEY = "point"; //$NON-NLS-1$
    public static final String PARENT_KEY = "parentKey"; //$NON-NLS-1$
    public static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$
    public static final String CHILDREN_KEY = "children"; //$NON-NLS-1$
}
