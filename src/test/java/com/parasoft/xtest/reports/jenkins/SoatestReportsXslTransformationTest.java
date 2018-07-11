/*
 * Copyright 2018 Parasoft Corporation
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

public class SoatestReportsXslTransformationTest
{

    private static final String SOATEST_XUNIT_XSL = "src/main/resources/com/parasoft/xtest/reports/jenkins/xunit/soatest-xunit.xsl";

    private static final String TEST_RESOURCES = "src/test/resources/";

    @Test
    public void testSoatest9DesktopFunctionalXUnitTransform()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatestDesktop9Functional.xml", "soatestDesktop9Functional-output.xml");
    }

    @Test
    public void testSoatest9DesktopFunctionalIterationsXUnitTransform()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatestDesktop9FunctionalIterations.xml", "soatestDesktop9FunctionalIterations-output.xml");
    }

    /**
     * soatestDesktopCalc3.xml has complex structure, test suites contains tests and other test suites
     */
    @Test
    public void testSoatest9DesktopCalc3FunctionalXUnitTransform()
    {
        soatestXunitTransformation( TEST_RESOURCES + "xml/soatestDesktopCalc3.xml", "soatestDesktopCalc3-output.xml");
    }

    @Test
    public void testSoatestDesktop9FunctionalOutputXUnit()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/soatestDesktop9Functional.xml";
            String outputFileName = "soatestDesktop9Functional-output.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, 2,6,6);
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testSoatestDesktop9FunctionalIterationsOutputXUnit()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/soatestDesktop9FunctionalIterations.xml";
            String outputFileName = "soatestDesktop9FunctionalIterations-output.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, 2,20,20);
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testSoatest9DesktopCalc3OutputXUnit()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/soatestDesktopCalc3.xml";
            String outputFileName = "soatestDesktopCalc3-output.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, 1,5,2);
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }


    @Test
    public void testSoatestWarFunctionalXUnitTransform()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatestWar.xml", "soatestWar-output.xml");
    }

    @Test
    public void testSoatestWarLoopFunctionalXUnitTransform()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatestWarLoop.xml", "soatestWarLoop-output.xml");
    }

    /**
     * soatestWarCalc3.xml has complex structure, test suites contains tests and other test suites
     */
    @Test
    public void testSoatestWarCalc3FunctionalXUnitTransform()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatestWarCalc3.xml", "soatestWarCalc3-output.xml");
    }


    @Test
    public void testSoatestWarFunctionalXUnitOutput()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/soatestWar.xml";
            String outputFileName = "soatestWar-output.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, 2,6,6);
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testSoatestWarLoopFunctionalXUnitOutput()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/soatestWarLoop.xml";
            String outputFileName = "soatestWarLoop-output.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, 2,20,20);
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testSoatestWarCalc3FunctionalXUnitOutput()
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/soatestWarCalc3.xml";
            String outputFileName = "soatestWarCalc3-output.xml";
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, 1,5,2);
        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    private void assertElementsCounts(TagCounterVerifier verifier, int expectedTestSuiteCount, int expectedTestCaseCount, int expectedFailuresCount)
    {
        Assert.assertEquals(expectedTestSuiteCount, verifier.getNumber("testsuite"));
        Assert.assertEquals(expectedTestCaseCount, verifier.getNumber("testcase"));
        Assert.assertEquals(expectedFailuresCount, verifier.getNumber("failure"));
    }

    private static void soatestXunitTransformation(String reportFileName, String outputFileName) {
        XUnitTransformer.testXUnitTransformation(reportFileName, outputFileName, SOATEST_XUNIT_XSL);
    }
}