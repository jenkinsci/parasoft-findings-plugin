/*
 * Copyright 2017 Parasoft Corporation
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
import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.reports.jenkins.parser.PathElementAnnotation.EmptyFlowAnalysisElement;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement.Type;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IResultLocation;

/** 
 * Creates complete path information for a flow analysis violation
 */
public class FlowAnalysisPathBuilder
{
    private final IFlowAnalysisViolation _violation;

    private final long _parentKey;

    /**
     * @param violation for which path information is needed
     * @param parentKey unique key of a warning that owns this violation
     */
    public FlowAnalysisPathBuilder(IFlowAnalysisViolation violation, long parentKey)
    {
        _violation = violation;
        _parentKey = parentKey;
    }

    /**
     * Returns path for flow analysis violation described by multiple elements.
     * @return multiple elements path information for flow analysis violation
     */
    public List<PathElementAnnotation> getPath()
    {
        return getPath(_violation.getPathElements());
    }

    private List<PathElementAnnotation> getPath(IFlowAnalysisPathElement[] descriptors)
    {
        List<PathElementAnnotation> result = new ArrayList<PathElementAnnotation>();
        for (IFlowAnalysisPathElement descriptor : descriptors) {
            PathElementAnnotation element = createElement(descriptor);
            result.add(element);
        }
        return result;
    }

    private PathElementAnnotation createElement(IFlowAnalysisPathElement descriptor)
    {
        IResultLocation location = descriptor.getLocation();
        PathElementAnnotation element;
        if (location != null) {
            String message = getMessage(descriptor, false);
            element = new PathElementAnnotation(message, location, _parentKey);
            element.setChildren(getChildren(descriptor));
            element.setDescription(getDescription(descriptor));
            element.setType(descriptor.getType().getIdentifier());
        } else {
            element = new EmptyFlowAnalysisElement(_parentKey);
        }
        return element;
    }

    private String getMessage(IFlowAnalysisPathElement descriptor, boolean bFullDescription)
    {
        StringBuilder sb = new StringBuilder();
        addTypeMessage(sb, descriptor, bFullDescription);
        addTrackedMessage(sb, descriptor, TRACKED_VARIABLES_ATTR, bFullDescription);
        addTrackedMessage(sb, descriptor, TRACKED_COLLECTIONS_ATTR, bFullDescription);
        return sb.toString();
    }
    
    private List<PathElementAnnotation> getChildren(IFlowAnalysisPathElement descriptor)
    {
        return getPath(descriptor.getChildren());
        
    }
    
    private String getDescription(IFlowAnalysisPathElement descriptor)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IHtmlTags.NON_BREAKABLE_SPACE);
        sb.append(IHtmlTags.CODE_START_TAG);
        sb.append(descriptor.getDescription());
        sb.append(IHtmlTags.CODE_END_TAG);
        String message = getMessage(descriptor, true);
        if (message.length() > 0) {
            sb.append(IHtmlTags.DIAMOND_SEPARATOR);
            sb.append(message);
        }
        return sb.toString();
    }

    private void addTypeMessage(StringBuilder sb, IFlowAnalysisPathElement descriptor, boolean bFullDescription)
    {
        Type type = descriptor.getType();
        if (type == null) {
            return;
        }
        String identifier = type.getIdentifier();

        if (identifier.contains("!")) { //$NON-NLS-1$
            addCriticalFlowMessage(sb, descriptor, bFullDescription);
        }
        if (identifier.contains("P")) {  //$NON-NLS-1$
            addPointMessage(sb, bFullDescription);
        }
        if (identifier.contains("C")) {  //$NON-NLS-1$
            addCauseMessage(sb, bFullDescription);
        }
        if (identifier.contains("I")) {  //$NON-NLS-1$
            addRuleImportantPointMessage(sb, bFullDescription);
        }
        if (identifier.contains("E")) {  //$NON-NLS-1$
            addExceptionMessage(sb, descriptor, bFullDescription);
        }

    }
    
    private static void addCriticalFlowMessage(StringBuilder sb, IFlowAnalysisPathElement descriptor, boolean bFullDescription)
    {
        String criticalFlowMessage = descriptor.getChildren().length == 0 ? CRITICAL_FLOW_MESSAGE : CONTAINS_CRITICAL_FLOW_MESSAGE;
        addBoldMessage(sb, criticalFlowMessage, bFullDescription);
    }

    private void addPointMessage(StringBuilder sb, boolean bFullDescription)
    {
        String pointMessage = _violation.getPointMessage();
        addBoldMessage(sb, pointMessage, bFullDescription);
    }

    private void addCauseMessage(StringBuilder sb, boolean bFullDescription)
    {
        String causeMessage = _violation.getCauseMessage();
        addBoldMessage(sb, causeMessage, bFullDescription);
    }
    
    private void addRuleImportantPointMessage(StringBuilder sb, boolean bFullDescription)
    {
        String ruleImportantMessage = _violation.getRuleImportantPointMessage();
        addBoldMessage(sb, ruleImportantMessage, bFullDescription);
    }
    
    private static void addBoldMessage(StringBuilder sb, String message, boolean bFullDescription)
    {
        if (UString.isEmpty(message)) {
            return;
        }
        if (sb.length() > 0) {
            String sSeparator = bFullDescription ? IHtmlTags.DIAMOND_SEPARATOR : "; "; //$NON-NLS-1$
            sb.append(sSeparator);
        }
        if (bFullDescription) {
            sb.append(IHtmlTags.BOLD_START_TAG);
        }
        sb.append(message);
        if (bFullDescription) {
            sb.append(IHtmlTags.BOLD_END_TAG);
        }
    }
    
    private static void addExceptionMessage(StringBuilder sb, IFlowAnalysisPathElement descriptor, boolean bFullDescription)
    {
        String throwingMethod = descriptor.getThrowingMethod();
        String thrownTypes = descriptor.getThrownTypes();
        if (UString.isEmpty(throwingMethod) || UString.isEmpty(thrownTypes)) {
            return;
        }
        if (sb.length() > 0) {
            String sSeparator = bFullDescription ? IHtmlTags.DIAMOND_SEPARATOR : "; "; //$NON-NLS-1$
            sb.append(sSeparator);
        }
        sb.append(throwingMethod);
        sb.append("() throws: ");   //$NON-NLS-1$
        sb.append(thrownTypes);
    }
    
    private void addTrackedMessage(StringBuilder sb, IFlowAnalysisPathElement descriptor, String attribute, boolean bFullDescription)
    {
        String message = _violation.getTrackedVariablesMessages().get(attribute);
        String variables = descriptor.getProperties().get(attribute);

        if (UString.isEmpty(variables)) {
            return;
        }
        String sSeparator = bFullDescription ? IHtmlTags.DIAMOND_SEPARATOR : "; "; //$NON-NLS-1$
        if (sb.length() > 0) {
            sb.append(sSeparator);
        }
        if (bFullDescription) {
        	sb.append(IHtmlTags.GRAY_ITALIC_STYLE_START_TAG);
        }
        sb.append(message);
        sb.append(IStringConstants.COLON_SP);
        sb.append(variables);
        if (bFullDescription) {
        	sb.append(IHtmlTags.ITALIC_END_TAG);
        }
    }

    private static final String TRACKED_VARIABLES_ATTR = "Tracked variables"; //$NON-NLS-1$

    private static final String TRACKED_COLLECTIONS_ATTR = "Tracked collections"; //$NON-NLS-1$

    private static final String CONTAINS_CRITICAL_FLOW_MESSAGE = "Contains critical data flow";   //$NON-NLS-1$

    private static final String CRITICAL_FLOW_MESSAGE = "Critical data flow";   //$NON-NLS-1$

}
