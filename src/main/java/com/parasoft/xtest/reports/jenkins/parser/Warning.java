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

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.util.List;
import java.util.NoSuchElementException;

import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.ParasoftDescriptor;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.results.api.IDupCodeViolation;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IRuleViolation;

/**
 * Core class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 * 
 */
public class Warning
    extends AbstractAnnotation
{
    private List<PathElementAnnotation> _children = null;

    private String _tooltip = null;

    private String _author = null;

    private String _revision = null;
    
    private String _analyzer = null;

    public Warning(Priority priority, String message, int startingLine, int endingLine, String ruleCategory, String ruleType)
    {
        super(priority, appendLineSeparator(message), startingLine, endingLine, ruleCategory, ruleType);
        
        setOrigin(ORIGIN);
    }

    /**
     * Java bean getter.
     * @return the author of this warning
     */
    public String getAuthor()
    {
        return _author;
    }

    /**
     * Java bean setter
     * @param author the author of this warning
     */
    public void setAuthor(String author)
    {
        _author = author;
    }

    /**
     * Java bean getter.
     * @return the revision associated with this warning
     */
    public String getRevision()
    {
        return _revision;
    }

    public String getLocalDocLink()
    {
        return "doc." + getAnalyzer() + '|' + getType(); //$NON-NLS-1$
    }

    /**
     * Java bean setter
     * @param revision the revision associated with this warning
     */
    public void setRevision(String revision)
    {
        _revision = revision;
    }

    /**
     * Java bean getter.
     * @return the analyzer which reported this warning
     */
    public String getAnalyzer()
    {
        return _analyzer;
    }

    /**
     * Java bean setter
     * @param analyzer the analyzer which reported this warning
     */
    public void setAnalyzer(String analyzer)
    {
        _analyzer = analyzer;
    }

    /**
     * Sets the base tooltip for this warning
     * @param sToolTip tooltip for this warning
     */
    public void setToolTip(String sToolTip)
    {
        _tooltip = UString.getNotNull(sToolTip);
    }

    @Override
    public String getToolTip()
    {
        return _tooltip;
    }

    /**
     * If given violation has description of detailed path elements - this method sets details regarding these elements.
     * You are supposed to call this method only once per instance.
     * 
     * @param violation the violation for which build path elements
     */
    public void populateViolationPathElements(IRuleViolation violation)
    {
        if (violation instanceof IFlowAnalysisViolation) {
            _children = new FlowAnalysisPathBuilder((IFlowAnalysisViolation)violation, getKey()).getPath();
            addTraceToolTip();
        } else if (violation instanceof IDupCodeViolation) {
            _children = new DupCodePathBuilder((IDupCodeViolation)violation, getKey()).getPath();
            addTraceToolTip();
        }
    }

    /**
     * Seeks call previous to given child. All child levels are checked.
     * 
     * @param childHashCode the child hash code
     * @return the previous call
     */
    public FileAnnotation getPreviousCall(long childHashCode)
    {
        for (PathElementAnnotation child : _children) {
            FileAnnotation prevCall = child.getPreviousCall(childHashCode);
            if (prevCall != null) {
                return prevCall;
            }

        }
        throw new NoSuchElementException("Previous call annotation not found: key=" + childHashCode); //$NON-NLS-1$
    }

    /**
     * @see hudson.plugins.analysis.util.model.AbstractAnnotation#compareTo(hudson.plugins.analysis.util.model.FileAnnotation)
     */
    public int compareTo(final FileAnnotation other)
    {
        int result;

        result = getFileName().compareTo(other.getFileName());
        if (result != 0) {
            return result;
        }
        result = getPrimaryLineNumber() - other.getPrimaryLineNumber();
        if (result != 0) {
            return result;
        }

        result = getMessage().compareTo(other.getMessage());
        if (result != 0) {
            return result;
        }
        if (this.equals(other)) {
            return 0;
        }
        Logger.getLogger().debug("Annotations compared by keys"); //$NON-NLS-1$
        long thisKey = getKey();
        long otherKey = other.getKey();
        return Long.compare(thisKey, otherKey);
    }
    

    /**
     * Adds call hierarchy to current tooltip.
     */
    private void addTraceToolTip()
    {
        StringBuilder message = new StringBuilder(_tooltip);
        message.append(getCallHierarchy());
        _tooltip = message.toString();
    }
    
    private String getCallHierarchy()
    {
        StringBuilder message = new StringBuilder();
        message.append("<ul>"); //$NON-NLS-1$
        for (PathElementAnnotation child : _children) {
            message.append(child.getChildrenLinks());
        }
        message.append("</ul>"); //$NON-NLS-1$
        return message.toString();
    }
    
    private static String appendLineSeparator(String message)
    {
        //used as hint in Jenkins is not showing correctly
        return message + IHtmlTags.LINE_SEPARATOR;
    }

    /** Unique identifier of this class. */
    private static final long serialVersionUID = 3626343302732656530L;

    static final String ORIGIN = ParasoftDescriptor.PLUGIN_ID;

}
