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

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.common.api.IFileTestableInput;
import com.parasoft.xtest.common.api.ISourceRange;
import com.parasoft.xtest.common.api.ITestableInput;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.results.api.IResultLocation;

/** 
 * Annotates a single path element.
 */
class PathElementAnnotation
    extends AbstractAnnotation
{
    
    private final long _parentKey;

    private List<PathElementAnnotation> _children = new ArrayList<PathElementAnnotation>();
    
    private String _description = null;

    private String _type = null;

    /**
     * @param message this element's message
     * @param location where this annotation exaclty is
     * @param parentKey unique key of a this element's parent
     */
    PathElementAnnotation(String message, IResultLocation location, long parentKey)
    {
        this(message, location.getSourceRange(), parentKey);
        
        setFileName(location);
    }
    
    private PathElementAnnotation(String message, ISourceRange sourceRange, long parentKey)
    {
        this(message, sourceRange.getStartLine(), sourceRange.getEndLine(), parentKey);
    }
    
    private PathElementAnnotation(String message, int startLine, int endLine, long parentKey)
    {
        super(message, startLine, endLine, IStringConstants.EMPTY, "FA"); //$NON-NLS-1$
        
        _parentKey = parentKey;
        setOrigin(Warning.ORIGIN);
    }

    /**
     * @param description info about this element
     */
    public void setDescription(String description)
    {
        _description = description;
    }
    
    /**
     * @param children child path elements for this one
     */
    public void setChildren(List<PathElementAnnotation> children)
    {
        _children = children;
    }
    
    /**
     * @param type type of this path element
     */
    public void setType(String type)
    {
        _type = type;
    }

    /**
     * @see hudson.plugins.analysis.util.model.FileAnnotation#getToolTip()
     */
    public String getToolTip()
    {
        return IStringConstants.EMPTY;
    }

    /**
     * Find annotation describing element previous to given one
     * 
     * @param childHashCode
     * @return corresponding annotation for previous child element or null
     */
    public FileAnnotation getPreviousCall(long childHashCode)
    {
        if (getKey() == childHashCode) {
            return this;
        }
        for (PathElementAnnotation child : _children) {
            if (child.getKey() == childHashCode) {
                return child;
            }
            FileAnnotation prevCall = child.getPreviousCall(childHashCode);
            if (prevCall != null) {
                return prevCall;
            }
        }
        return null;
    }

    protected String getChildrenLinks()
    {
        StringBuilder message = new StringBuilder();
        message.append(IHtmlTags.LIST_ELEM_START_TAG);
        addType(message);
        message.append(getLinkToCallPlace());
        message.append(IHtmlTags.NON_BREAKABLE_SPACE);
        message.append(_description);
        addChildren(message);
        message.append(IHtmlTags.LIST_ELEM_END_TAG);
        return message.toString();
    }
    
    protected String getLinkToCallPlace()
    {
        return String.format(
            "<a href=\"link.%s.%s/#%s\">%s (%s):</a>", //$NON-NLS-1$
            _parentKey, getKey(), getPrimaryLineNumber(), getShortFileName(),
            getPrimaryLineNumber());
    }

    private void addType(StringBuilder message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IHtmlTags.FONT_MONOSPACE_SPAN_START_TAG);
        sb.append(_type);
        int length = _type.length();
        for (int i = 0; i < 4 - length; i++) {
            sb.append(IHtmlTags.NON_BREAKABLE_SPACE);
        }
        sb.append(IHtmlTags.SPAN_END_TAG);
        message.append(sb.toString());
    }

    private void addChildren(StringBuilder message)
    {
        for (PathElementAnnotation child : _children) {
            message.append(IHtmlTags.LIST_START_TAG);
            message.append(child.getChildrenLinks());
            message.append(IHtmlTags.LIST_END_TAG);
        }
    }
    
    private void setFileName(IResultLocation location) // parasoft-suppress PB.CUB.IMC "intended"
    {
        ITestableInput input = location.getTestableInput();
        if (input instanceof IFileTestableInput) {
            setFileName(((IFileTestableInput)input).getFileLocation().getAbsolutePath());
        } else {
            if (input != null) {
                setFileName(input.getName());
            }
        }
    }
    
    private static final long serialVersionUID = 7363385638416660257L;

    static final class EmptyFlowAnalysisElement extends PathElementAnnotation
    {
        private static final long serialVersionUID = 1099083435543952452L;
        
        public EmptyFlowAnalysisElement(long parentKey)
        {
            super(IStringConstants.EMPTY, 0, 0, parentKey);
            setChildren(Collections.<PathElementAnnotation>emptyList());
            setDescription(IStringConstants.EMPTY);
            setType(IStringConstants.EMPTY);
        }

        @Override
        protected String getLinkToCallPlace()
        {
            return IStringConstants.ELLIPSIS;
        }

    }

}
