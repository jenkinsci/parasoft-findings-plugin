/*
 * Copyright 2016 Parasoft Corporation
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

import java.util.ArrayList;
import java.util.List;

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.reports.jenkins.parser.PathElementAnnotation.EmptyFlowAnalysisElement;
import com.parasoft.xtest.results.api.IDupCodeViolation;
import com.parasoft.xtest.results.api.IPathElement;
import com.parasoft.xtest.results.api.IResultLocation;

/** 
 * Creates complete path information for a code duplication violation
 */
public class DupCodePathBuilder
{
    private final IDupCodeViolation _violation;

    private final long _parentKey;
    
    /**
     * @param violation for which path information is needed
     * @param parentKey unique key of a warning that owns this violation
     */
    public DupCodePathBuilder(IDupCodeViolation violation, long parentKey)
    {
        _violation = violation;
        _parentKey = parentKey;
    }
    
    /**
     * Returns path for code duplication violation described by multiple elements.
     * @return multiple elements path information for code duplication violation
     */
    public List<PathElementAnnotation> getPath()
    {
        return getPath(_violation.getPathElements());
    }
    
    private List<PathElementAnnotation> getPath(IPathElement[] aPathElements)
    {
        List<PathElementAnnotation> result = new ArrayList<PathElementAnnotation>();
        for (IPathElement pathElement : aPathElements) {
            PathElementAnnotation element = createElement(pathElement);
            result.add(element);
        }
        return result;
    }
    
    private PathElementAnnotation createElement(IPathElement pathElement)
    {
        IResultLocation location = pathElement.getLocation();
        PathElementAnnotation element;
        if (location != null) {
            String sMessage = getMessage(pathElement);
            element = new PathElementAnnotation(sMessage, location, _parentKey);
            element.setDescription(getDescription(pathElement));
            element.setType(IStringConstants.EMPTY);
        } else {
            element = new EmptyFlowAnalysisElement(_parentKey);
        }
        return element;
    }
    
    private static String getDescription(IPathElement pathElement)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IHtmlTags.NON_BREAKABLE_SPACE);
        sb.append(IHtmlTags.CODE_START_TAG);
        sb.append(pathElement.getDescription());
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
