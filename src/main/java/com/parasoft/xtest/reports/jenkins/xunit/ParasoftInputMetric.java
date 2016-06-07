/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.xtest.reports.jenkins.xunit;

import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;

public class ParasoftInputMetric
    extends InputMetricXSL
{
    @Override
    public InputType getToolType()
    {
        return InputType.TEST;
    }

    @Override
    public String getToolName()
    {
        return PARASOFT_TOOL;  
    }

    @Override
    public String getToolVersion()
    {
        return VERSION;  
    }

    @Override
    public String getXslName()
    {
        return XUNIT_XSL;  
    }

    @Override
    public String[] getInputXsdNameList() // parasoft-suppress PB.EAR "Reviewed"
    {
        return null;
    }

    @Override
    public OutputMetric getOutputFormatType()
    {
        return JUnitModel.LATEST;
    }
    
    private static final String PARASOFT_TOOL = "ParasoftTest"; //$NON-NLS-1$
    private static final String VERSION = "10.x"; //$NON-NLS-1$
    private static final String XUNIT_XSL = "xunit.xsl"; //$NON-NLS-1$
    
    private static final long serialVersionUID = -5284309737798604284L;
}
