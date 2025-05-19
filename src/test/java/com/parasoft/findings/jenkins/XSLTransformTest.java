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

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XSLTransformTest {

    private static final String TEST_RESOURCES = "src/test/resources/";

    private static final String XUNIT_XSL = "src/main/resources/com/parasoft/findings/jenkins/xunit/xunit.xsl";

    /**
     * @task 66687
     */
    @Test
    void testXUnitTransform() {
        xunitTransformation(TEST_RESOURCES + "xml/jTest_10_unit.xml", "junit-jtest.xml");
        xunitTransformation(TEST_RESOURCES + "xml/simulator_unit.xml", "junit-sim.xml");
    }

    @Test
    void testCppTest10EngineXUnitTransform2() {
        xunitTransformation(TEST_RESOURCES + "xml/cppTest_10.3.4_engine_unit_2.xml", "cppTest_10.3.4_engine_unit_2-output.xml");
    }

    @Test
    void testCppTest10EngineXUnitTransform() {
        xunitTransformation(TEST_RESOURCES + "xml/cppTest_10.3.3_desktop_vs_unit.xml", "cppTest_10.3.3_desktop_vs_unit-output.xml");
    }

    /**
     * @task 88025
     */
    @Test
    void testDemoXUnitTransform() {
        transform("jTest_10.2_unit.xml", 4, 4);
    }

    @Test
    void testCppTestUnitXUnitTransform() {
        transform("cppTest_10.3.3_desktop_unit.xml", 147, 0);
    }

    @Test
    void testCppTestUnit_10_5_1_XUnitTransform() {
        transform("cppTest_10.5.1_unit.xml", 15, 0);
    }

    @Test
    void testCppTestUnit_10_5_2_XUnitTransform() {
        transform("cppTest_10.5.2_unit.xml", 15, 0);
    }

    @Test
    void testCppTestUnit_10_6_0_XUnitTransform() {
        transform("cppTest_10.6.0_unit.xml", 27, 0);
    }

    @Test
    void testCppTesEngineUnitXUnitTransform() {
        transform("cppTest_10.3.4_engine_unit.xml", 12, 5);
    }

    private void transform(String fileName, int failureNumber, int errorNumber) {
        try {
            String reportFileName = TEST_RESOURCES + "xml/" + fileName;
            String outputFileName = "junit-demo.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertEquals(failureNumber, verifier.getNumber("failure"));
            assertEquals(errorNumber, verifier.getNumber("error"));
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    private static void xunitTransformation(String reportFileName, String outputFileName) {
        XUnitTransformer.testXUnitTransformation(reportFileName, outputFileName, XUNIT_XSL);
    }
}