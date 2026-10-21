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
package com.parasoft.findings.jenkins;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SoatestReportsXslTransformationTest
{

    private static final String SOATEST_XUNIT_XSL = "src/main/resources/com/parasoft/findings/jenkins/xunit/soatest-xunit.xsl";

    private static final String TEST_RESOURCES = "src/test/resources/";

    private static final String EXECUTION_VIOLATIONS_REPORT = "SOAtest_functional_execution_violations.xml";

    private static final String[] EXECUTION_VIOLATION_TYPES = {
        "ExecViol", "UnitViol", "FuncViol", "RuntimeViol", "ChangeImpact", "VisualDifference"
    };

    @Test
    public void testSoatestExecutionViolationsForSingleTestCases() throws Exception
    {
        Document output = transformExecutionViolations("execution-violations-single-output.xml");
        for (String type : EXECUTION_VIOLATION_TYPES) {
            Element testCase = findTestCase(output, "Execution violations/Single " + type);
            assertExecutionViolation(testCase, type);
            Assert.assertEquals("1.000", testCase.getAttribute("time"));
        }
        Assert.assertEquals(0, findTestCase(output, "Execution violations/Passing test")
                .getElementsByTagName("failure").getLength());
    }

    @Test
    public void testSoatestExecutionViolationsForMultipleTestCases() throws Exception
    {
        Document output = transformExecutionViolations("execution-violations-multiple-output.xml");
        for (String type : EXECUTION_VIOLATION_TYPES) {
            assertExecutionViolation(findTestCase(output, "Execution violations/Parameterized " + type + "[Safari]"), type);
            Assert.assertEquals(type, 0, findTestCase(output, "Execution violations/Parameterized " + type + "[Chrome]")
                    .getElementsByTagName("failure").getLength());
        }
    }

    @Test
    public void testSoatestMixedExecutionViolationDetails() throws Exception
    {
        Document output = transformExecutionViolations("execution-violations-mixed-output.xml");
        NodeList failures = findTestCase(output, "Execution violations/Mixed violations").getElementsByTagName("failure");
        Assert.assertEquals(1, failures.getLength());
        Element failure = (Element) failures.item(0);
        Assert.assertEquals("Multiple errors reported", failure.getAttribute("message"));
        Assert.assertEquals("Functional failure\n\nChange impact failure\nChange impact details"
                + "\n\nVisual difference failure\nSource: screenshot.png", failure.getTextContent());
    }

    @Test
    public void testSoatestExecutionViolationWithoutTestCase() throws Exception
    {
        Document output = transformExecutionViolations("execution-violations-without-test-case-output.xml");
        NodeList failures = findTestCase(output, "Execution violations/Without test case").getElementsByTagName("failure");
        Assert.assertEquals(1, failures.getLength());
        Assert.assertEquals("Visual comparison failed", ((Element) failures.item(0)).getAttribute("message"));
    }

    @Test
    public void testSoatestExecutionViolationCounts() throws Exception
    {
        transform(EXECUTION_VIOLATIONS_REPORT, "execution-violations-counts-output.xml", 1, 21, 14);
        Document output = transformExecutionViolations("execution-violations-totals-output.xml");
        Element suite = (Element) output.getElementsByTagName("testsuite").item(0);
        Assert.assertEquals("21", suite.getAttribute("tests"));
        Assert.assertEquals("14", suite.getAttribute("failures"));
    }

    @Test
    public void testSoatestExecutionViolationsOutputSchema()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/" + EXECUTION_VIOLATIONS_REPORT,
                "execution-violations-schema-output.xml");
    }

    private Document transformExecutionViolations(String outputFileName) throws Exception
    {
        File output = XUnitTransformer.transform(TEST_RESOURCES + "xml/" + EXECUTION_VIOLATIONS_REPORT,
                outputFileName, SOATEST_XUNIT_XSL);
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(output);
    }

    private Element findTestCase(Document output, String name) throws Exception
    {
        Element testCase = (Element) XPathFactory.newInstance().newXPath()
                .evaluate("//testcase[@name='" + name + "']", output, XPathConstants.NODE);
        Assert.assertNotNull("Missing test case: " + name, testCase);
        return testCase;
    }

    private void assertExecutionViolation(Element testCase, String type)
    {
        NodeList failures = testCase.getElementsByTagName("failure");
        Assert.assertEquals(type, 1, failures.getLength());
        Element failure = (Element) failures.item(0);
        Assert.assertEquals(type + " failure", failure.getAttribute("message"));
        Assert.assertEquals("RuntimeViol".equals(type) ? "" : type + " details", failure.getTextContent());
    }

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
    public void testSoatest_10_6_1_FunctionalOutputXUnit()
    {
        transform("SOAtest_functional_10.6.1.xml", "soatest_10_6_1_Functional-output.xml", 1, 18, 18);
    }

    @Test
    public void testSoatest_2025_3_0_FunctionalOutputXUnit()
    {
        transform("SOAtest_functional_2025.3.0_zero_total_test.xml", "SOAtest_functional_2025.3.0-output.xml", 0, 0, 0);
    }

    @Test
    public void testSoatest_2025_3_0_no_ExecutedTestsDetails_tag_FunctionalOutputXUnit()
    {
        transform("SOAtest_functional_2025.3.0_no_ExecutedTestsDetails_tag.xml", "SOAtest_functional_2025.3.0_no_ExecutedTestsDetails_tag-output.xml", 0, 0, 0);
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
