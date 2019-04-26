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

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;

public class DupIssueAdditionalProperties
    extends ParasoftIssueAdditionalProperties
{
    private static final long serialVersionUID = -984630394099766160L;

    private String _parentKey;
    private List<Issue> _children;
    private String _description;

    public DupIssueAdditionalProperties()
    {}

    public DupIssueAdditionalProperties(String author, String revision, String analyzer)
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
        DupIssueAdditionalProperties additionalProperties = getAdditionalProperties(issue);
        message.append(IHtmlTags.LIST_ELEM_START_TAG);
        message.append(getLinkToCallPlace(issue, additionalProperties, fileNameRenderer));
        message.append(IHtmlTags.NON_BREAKABLE_SPACE);
        message.append(additionalProperties.getDescription());
        message.append(IHtmlTags.LIST_ELEM_END_TAG);
        return message.toString();
    }

    private String getLinkToCallPlace(Issue issue, DupIssueAdditionalProperties additionalProperties, FileNameRenderer fileNameRenderer)
    {
        if (fileNameRenderer != null) {
            return fileNameRenderer.createAffectedFileLink(issue).render();
        } else {
            return IStringConstants.EMPTY;
        }
    }

    private DupIssueAdditionalProperties getAdditionalProperties(Issue issue)
    {
        Serializable properties = issue.getAdditionalProperties();
        if (properties instanceof DupIssueAdditionalProperties) {
            return (DupIssueAdditionalProperties) properties;
        }
        return null;
    }
}
