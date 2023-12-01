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

import static org.junit.Assert.assertEquals;

import com.parasoft.findings.jenkins.internal.variables.JenkinsVariablesResolver;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JenkinsVariablesResolverTest
{
    private static final String PATH_EXAMPLE = "/job/workspace/builds/";

    @Test
    public void testResolveSurroundedByCurlyBrackets()
    {
        String sVariable = "${BUILD_NUMBER}";
        String sVariable2 = "${build_number}";
        String sExpr = PATH_EXAMPLE + sVariable;
        String sExpr2 = sVariable + PATH_EXAMPLE;
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("BUILD_NUMBER", "1");

        JenkinsVariablesResolver resolver = new JenkinsVariablesResolver(variables);
        assertEquals("1", resolver.performSubstitution(sVariable));
        assertEquals(sVariable2, resolver.performSubstitution(sVariable2));
        assertEquals(PATH_EXAMPLE + "1", resolver.performSubstitution(sExpr));
        assertEquals("1" + PATH_EXAMPLE, resolver.performSubstitution(sExpr2));
    }

    @Test
    public void testResolveSurroundedByPercentSign()
    {
        String sVariable = "%BUILD_NUMBER%";
        String sExpr = PATH_EXAMPLE + sVariable;
        String sExpr2 = sVariable + PATH_EXAMPLE;
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("BUILD_NUMBER", "1");

        JenkinsVariablesResolver resolver = new JenkinsVariablesResolver(variables);
        assertEquals("1", resolver.performSubstitution(sVariable));
        assertEquals(PATH_EXAMPLE + "1", resolver.performSubstitution(sExpr));
        assertEquals("1" + PATH_EXAMPLE, resolver.performSubstitution(sExpr2));
    }

}
