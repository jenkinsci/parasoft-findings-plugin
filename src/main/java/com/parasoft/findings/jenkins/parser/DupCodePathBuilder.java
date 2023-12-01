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

import java.util.ArrayList;
import java.util.List;

import com.parasoft.findings.utils.common.IStringConstants;
import com.parasoft.findings.utils.common.util.StringUtil;
import com.parasoft.findings.utils.results.testableinput.IFileTestableInput;
import com.parasoft.findings.utils.results.testableinput.ProjectFileTestableInput;
import com.parasoft.findings.utils.results.violations.SourceRange;
import com.parasoft.findings.utils.results.testableinput.ITestableInput;
import com.parasoft.findings.utils.results.testableinput.PathInput;
import com.parasoft.findings.jenkins.html.Colors;
import com.parasoft.findings.jenkins.html.IHtmlTags;
import com.parasoft.findings.utils.results.testableinput.FindingsLocationMatcher;
import com.parasoft.findings.utils.results.violations.IPathElement;

import com.parasoft.findings.utils.results.violations.ResultLocation;
import com.parasoft.findings.utils.results.violations.DupCodeViolation;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;

public class DupCodePathBuilder
{
    private final DupCodeViolation _violation;
    private final String _parentKey;

    public DupCodePathBuilder(DupCodeViolation violation, String parentKey)
    {
        _violation = violation;
        _parentKey = parentKey;
    }

    public List<Issue> getPath()
    {
        return getPath(_violation.getPathElements());
    }

    private List<Issue> getPath(IPathElement[] aPathElements)
    {
        List<Issue> result = new ArrayList<Issue>();
        IssueBuilder issueBuilder = new IssueBuilder();
        for (IPathElement pathElement : aPathElements) {
            Issue element = createElement(pathElement, issueBuilder);
            result.add(element);
        }
        return result;
    }

    private Issue createElement(IPathElement pathElement, IssueBuilder issueBuilder)
    {
        ResultLocation location = pathElement.getLocation();
        DupIssueAdditionalProperties additionalProperties = new DupIssueAdditionalProperties();
        issueBuilder.setAdditionalProperties(additionalProperties);
        if (location != null) {
            String message = getMessage(pathElement);
            issueBuilder.setMessage(message);
            ITestableInput input = location.getTestableInput();
            String filePath = null;
            if (input instanceof IFileTestableInput) {
                filePath = FindingsLocationMatcher.getFilePath((IFileTestableInput) input);
            } else if (input instanceof PathInput) {
                filePath = ((PathInput) input).getPath();
                if (filePath.startsWith("/")) { //$NON-NLS-1$
                    filePath = filePath.substring(1);
                }
            } else {
                filePath = input.getName();
            }
            if (StringUtil.isNonEmptyTrimmed(filePath)) {
                issueBuilder.setFileName(filePath);
            }

            if (input instanceof ProjectFileTestableInput) {
                ProjectFileTestableInput projectInput = (ProjectFileTestableInput) input;
                issueBuilder.setModuleName(projectInput.getProjectName());
            }

            SourceRange sourceRange = location.getSourceRange();
            issueBuilder.setLineStart(sourceRange.getStartLine());
            issueBuilder.setLineEnd(sourceRange.getEndLine());
            issueBuilder.setColumnStart(sourceRange.getStartLineOffset());
            issueBuilder.setColumnEnd(sourceRange.getEndLineOffset());

            additionalProperties.setDescription(getDescription(pathElement));
        }
        additionalProperties.setParentKey(_parentKey);
        return issueBuilder.build();
    }

    private static String getDescription(IPathElement pathElement)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IHtmlTags.NON_BREAKABLE_SPACE);
        sb.append(IHtmlTags.CODE_START_TAG);
        sb.append(Colors.createColorSpanStartTag(Colors.BLACK));
        sb.append(pathElement.getDescription());
        sb.append(IHtmlTags.SPAN_END_TAG);
        sb.append(IHtmlTags.CODE_END_TAG);
        return sb.toString();
    }

    private static String getMessage(IPathElement pathElement)
    {
        String sDescription = pathElement.getDescription();
        if (sDescription == null) {
            sDescription = IStringConstants.EMPTY;
        }
        return sDescription;
    }
}
