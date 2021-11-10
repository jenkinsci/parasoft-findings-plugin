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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

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
        transform("soatestDesktop9Functional.xml", "soatestDesktop9Functional-output.xml", 2, 6, 6);
    }

    @Test
    public void testSoatestDesktop10FunctionalOutputXUnit()
    {
        transform("soatest_desktop_10_5_2.xml", "soatestDesktop10Functional-output.xml", 3, 6, 6);
    }

    @Test
    public void testSoatestDesktop9FunctionalIterationsOutputXUnit()
    {
        transform("soatestDesktop9FunctionalIterations.xml", "soatestDesktop9FunctionalIterations.xml", 2, 20, 20);
    }

    @Test
    public void testSoatest9DesktopCalc3OutputXUnit()
    {
        transform("soatestDesktopCalc3.xml", "soatestDesktopCalc3-output.xml", 1, 5, 2);
    }

    private void transform(String fileName, String outputFileName, int testsuite, int testcase, int failure)
    {
        try {
            String reportFileName = TEST_RESOURCES + "xml/" + fileName;
            File outputFile = XUnitTransformer.transform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);

            TagCounterVerifier verifier = new TagCounterVerifier();
            XUnitTransformer.parseXunitOutputXml(outputFile, verifier);

            assertElementsCounts(verifier, testsuite, testcase, failure);
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
        transform("soatestWar.xml", "soatestWar-output.xml", 2, 6, 6);
    }

    @Test
    public void testSoatestWarLoopFunctionalXUnitOutput()
    {
        transform("soatestWarLoop.xml", "soatestWarLoop-output.xml", 2, 20, 20);
    }

    @Test
    public void testSoatestWarCalc3FunctionalXUnitOutput()
    {
        transform("soatestWarCalc3.xml", "soatestWarCalc3-output.xml", 1, 5, 2);
    }

    @Test
    public void parseSOAtestTest_10_5_XUnitOutput()
    {
        transform("soatest_10.5.0.xml", "soatest_10.5.0-output.xml", 12, 153, 82);
    }

    @Test
    public void parseSOAtestTest_10_5_XUnitTransform()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatest_10.5.0.xml", "soatest_10.5.0-output.xml");
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