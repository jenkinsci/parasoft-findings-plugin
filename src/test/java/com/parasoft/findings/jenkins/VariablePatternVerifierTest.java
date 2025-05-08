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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parasoft.findings.jenkins.internal.variables.VariablePatternVerifier;

import org.junit.jupiter.api.Test;

class VariablePatternVerifierTest {
    /**
     * @task 60983
     * @pr 108878
     */
    @Test
    void testPercentPattern() {
        VariablePatternVerifier verifier = new VariablePatternVerifier("%BUILD_ID");
        assertTrue(verifier.containsVariables());
        assertFalse(verifier.checkVariableNotation());

        verifier = new VariablePatternVerifier("%build_id%");
        assertTrue(verifier.containsVariables());
        assertFalse(verifier.checkVariableNotation());

        verifier = new VariablePatternVerifier("%BUILD_ID%");
        assertTrue(verifier.containsVariables());
        assertTrue(verifier.checkVariableNotation());
    }

    /**
     * @task 60983
     * @pr 108878
     */
    @Test
    void testDollarPattern() {
        VariablePatternVerifier verifier = new VariablePatternVerifier("$BUILD_ID");
        assertTrue(verifier.containsVariables());
        assertFalse(verifier.checkVariableNotation());

        verifier = new VariablePatternVerifier("${build_id}");
        assertTrue(verifier.containsVariables());
        assertFalse(verifier.checkVariableNotation());

        verifier = new VariablePatternVerifier("${BUILD_ID}");
        assertTrue(verifier.containsVariables());
        assertTrue(verifier.checkVariableNotation());
    }

}
