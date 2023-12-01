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

package com.parasoft.findings.jenkins.internal.variables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.parasoft.findings.utils.common.variables.IVariablesProvider;
import com.parasoft.findings.utils.common.variables.StaticVariable;
import com.parasoft.findings.utils.common.variables.VariablesResolver;

/**
 * Resolver impl for jenkins variables in jenkins notation.
 */
public class JenkinsVariablesResolver
        extends VariablesResolver
{

    /**
     * @param buildVariables the map with build variables
     */
    public JenkinsVariablesResolver(Map<String, String> buildVariables)
    {
        super(new JenkinsVariablesProvider(buildVariables));
    }

    @Override
    public String performSubstitution(String sExpression)
    {
        String sModifiedExpression = prepare(sExpression);
        return super.performSubstitution(sModifiedExpression);
    }

    /**
     * @return supported jenkins variable names
     */
    public static Set<String> getResolvableVariables()
    {
        Set<String> result = new HashSet<String>();
        result.add("BUILD_ID");   //$NON-NLS-1$
        result.add("BUILD_NUMBER");   //$NON-NLS-1$
        result.add("BUILD_TAG");   //$NON-NLS-1$
        result.add("JOB_NAME");   //$NON-NLS-1$
        return result;
    }

    private static String prepare(String sExpression)
    {
        if (sExpression == null) {
            return null;
        }
        String sResult = sExpression;
        while (sResult.contains("%")) { //$NON-NLS-1$
            sResult = sResult.replaceFirst("%", "\\${"); //$NON-NLS-1$  //$NON-NLS-2$
            sResult = sResult.replaceFirst("%", "}"); //$NON-NLS-1$  //$NON-NLS-2$
        }
        return sResult;
    }

    private static class JenkinsVariablesProvider
            implements IVariablesProvider
    {
        private final Map<String, String> _variables = new HashMap<String, String>();

        public JenkinsVariablesProvider(Map<String, String> buildVariables)
        {
            _variables.putAll(buildVariables);
        }

        public StaticVariable getVariable(String sName)
        {
            String sValue = _variables.get(sName);
            return sValue == null ? null : new StaticVariable(sName, sValue);
        }

    }

}
