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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class XSLTransformTest
{
    private static final String TEST_RESOURCES = "src/test/resources/";

    private static final String XUNIT_XSL = "src/main/resources/com/parasoft/xtest/reports/jenkins/xunit/xunit.xsl";

    /**
     * @task 66687
     */
    @Test
    public void testXUnitTransform()
    {
        xunitTransformation(TEST_RESOURCES + "xml/jTest_10_unit.xml", "junit-jtest.xml");
        xunitTransformation(TEST_RESOURCES + "xml/simulator_unit.xml", "junit-sim.xml");
    }

    @Test
    public void testCppTest10EngineXUnitTransform2()
    {
        xunitTransformation(TEST_RESOURCES + "xml/c++Test10Engine.xml", "c++Test10Engine-output.xml");
    }

    @Test
    public void testCppTest10EngineXUnitTransform()
    {
        xunitTransformation(TEST_RESOURCES + "xml/c++Test10DesktopVisualStudio.xml", "c++Test10DesktopVisualStudio-output.xml");
    }

    /**
     * @task 88025
     */
    @Test
    public void testDemoXUnitTransform()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/jTest_10.2_unit.xml";
            String outputFileName = "junit-demo.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            Assert.assertEquals(4, verifier.getNumber("failure"));
            Assert.assertEquals(4, verifier.getNumber("error"));
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testCppTestUnitXUnitTransform()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/cppTest_10.3.3_unit.xml";
            String outputFileName = "junit-demo.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            Assert.assertEquals(147, verifier.getNumber("failure"));
            Assert.assertEquals(0, verifier.getNumber("error"));

        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testCppTesEngineUnitXUnitTransform()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/cppTest_10.3.4_Engine_unit.xml";
            String outputFileName = "junit-demo.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            Assert.assertEquals(12, verifier.getNumber("failure"));
            Assert.assertEquals(5, verifier.getNumber("error"));

        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    private static void xunitTransformation(String reportFileName, String outputFileName)
    {
        XUnitTransformer.testXUnitTransformation(reportFileName, outputFileName, XUNIT_XSL);
    }
}