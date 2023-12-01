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
package com.parasoft.findings.jenkins;

import com.parasoft.findings.utils.results.violations.IRuleViolation;
import com.parasoft.findings.utils.results.violations.ResultLocation;

import java.util.Properties;


public class RuleViolationMock implements IRuleViolation
{

    private ResultLocation _resultLocation;
    private String _packageName;
    private Properties _properties;
    private String _message;
    private String _ruleId;

    public String getAnalyzerId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNamespace()
    {
        return _packageName;
    }

    public ResultLocation getResultLocation()
    {
        return _resultLocation;
    }

    public void addAttribute(String sName, String sValue)
    {
        // TODO Auto-generated method stub

    }

    public String getAttribute(String sName)
    {
        return _properties.getProperty(sName);
    }

    public String getMessage()
    {
        return _message;
    }

    public String getRuleId()
    {
        return _ruleId;
    }

    public RuleViolationMock(ResultLocation resultLocation, String packageName,
                             Properties properties, String message, String ruleId)
    {
        super();
        _resultLocation = resultLocation;
        _packageName = packageName;
        _properties = properties;
        _message = message;
        _ruleId = ruleId;
    }

}
