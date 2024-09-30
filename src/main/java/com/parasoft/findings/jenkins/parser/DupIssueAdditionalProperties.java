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

import com.parasoft.findings.utils.common.IStringConstants;
import com.parasoft.findings.jenkins.html.IHtmlTags;

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;

public class DupIssueAdditionalProperties // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
        extends ParasoftIssueAdditionalProperties
{
    private static final long serialVersionUID = -984630394099766160L;

    public DupIssueAdditionalProperties()
    {}

    public DupIssueAdditionalProperties(String author, String revision, String analyzer)
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
        DupIssueAdditionalProperties additionalProperties = getAdditionalProperties(issue);
        if (additionalProperties == null) {
            return null;
        }
        StringBuilder message = new StringBuilder();

        message.append(IHtmlTags.LIST_ELEM_START_TAG);
        message.append(getLinkToCallPlace(issue, fileNameRenderer));

        message.append(IHtmlTags.NON_BREAKABLE_SPACE);
        message.append(additionalProperties.getDescription());

        message.append(IHtmlTags.LIST_ELEM_END_TAG);
        return message.toString();
    }

    private String getLinkToCallPlace(Issue issue, FileNameRenderer fileNameRenderer)
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

    public static final String PARENT_KEY = "parentKey"; //$NON-NLS-1$

    public static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$

    public static final String CHILDREN_KEY = "children"; //$NON-NLS-1$
}
