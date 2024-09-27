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

package com.parasoft.findings.jenkins.xunit;

import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;

public class ParasoftInputMetric // parasoft-suppress OWASP2021.A8.OROM "reviewed"
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
    
    private static final String PARASOFT_TOOL = "ParasoftAnalyzers"; //$NON-NLS-1$
    private static final String VERSION = "10.x"; //$NON-NLS-1$
    private static final String XUNIT_XSL = "xunit.xsl"; //$NON-NLS-1$
    
    private static final long serialVersionUID = -5284309737798604284L;
}
