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
package com.parasoft.xtest.reports.jenkins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.parasoft.xtest.results.api.IFlowAnalysisPathElement;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IResultLocation;

public class FARuleViolationMock
    extends RuleViolationMock
    implements IFlowAnalysisViolation
{
    public FARuleViolationMock(IResultLocation resultLocation, String packageName,
        Properties properties, String message, String ruleId, IFlowAnalysisPathElement[] FAElemDesc)
    {
        super(resultLocation, packageName, properties, message, ruleId);
        _FAElemDesc = FAElemDesc;
    }


    private IFlowAnalysisPathElement[] _FAElemDesc;

    public IFlowAnalysisPathElement[] getPathElements()
    {
        return _FAElemDesc;
    }

    public int getId()
    {
        return 0;
    }

    public IResultLocation getCauseLocation()
    {
        return null;
    }

    public String getCauseMessage()
    {
        return null;
    }

    public String getRuleImportantPointMessage()
    {
        return null;
    }

    public String getRuleHiddenMessage()
    {
        return null;
    }

    public String getPointMessage()
    {
        return null;
    }

    public Map<String, String> getTrackedVariablesMessages()
    {
        return new HashMap<String, String>();
    }


    public void setElementDescriptors(IFlowAnalysisPathElement[] FAElemDesc)
    {
        _FAElemDesc = FAElemDesc;
    }

    public String getFlowPathDescription()
    {
        return null;
    }

}
