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

    private static final String CHANGE_DETAILS = "Service Details (from the Service Catalog):\n"
            + "\tService Name:  http://localhost:18080/parabank/services/store-01V2?wsdl\n"
            + "\tSaved Service Version:  Old Version\n\tCurrent Service Version:  Current Version\n\n"
            + "Source: http://localhost:18081/current.wsdl";

    private static final String[] EXECUTION_VIOLATION_TYPES = {
        "FuncViol", "ChangeImpact", "VisualDifference"
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
        Assert.assertEquals("Functional failure"
                + "\n\nChange impact failure\nChange impact details"
                + "\n\nVisual difference failure\nSource: screenshot.png"
                + "\n\nFuncViol\nMixed details without summary", failure.getTextContent());
    }

    @Test
    public void testSoatestExecutionViolationsWithoutMessages() throws Exception
    {
        Document output = transformExecutionViolations("execution-violations-message-fallback-output.xml");
        assertFailure(output, "Functional without message", "FuncViol", "Details without summary");
    }

    @Test
    public void testSoatestUnknownExecutionViolationIsNotReportedAsPassing() throws Exception
    {
        // FutureViolation is synthetic: matching must not depend on a fixed list of known types.
        Document output = transformExecutionViolations("execution-violations-unknown-output.xml");
        assertFailure(output, "Unknown violation", "Future violation failure", "Future violation details");
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
        transform(EXECUTION_VIOLATIONS_REPORT, "execution-violations-counts-output.xml", 1, 14, 10);
        Document output = transformExecutionViolations("execution-violations-totals-output.xml");
        Element suite = (Element) output.getElementsByTagName("testsuite").item(0);
        Assert.assertEquals("14", suite.getAttribute("tests"));
        Assert.assertEquals("10", suite.getAttribute("failures"));
    }

    @Test
    public void testSoatestExecutionViolationsOutputSchema()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/" + EXECUTION_VIOLATIONS_REPORT,
                "execution-violations-schema-output.xml");
    }

    @Test
    public void testOriginalVisualDifferenceReport() throws Exception
    {
        Document output = transform("soatest_visual_difference.xml", "native-original-visual-output.xml");
        assertCounts(output, 129, 526, 1);
        NodeList testCases = output.getElementsByTagName("testcase");
        int passingTests = 0;
        for (int i = 0; i < testCases.getLength(); i++) {
            Element testCase = (Element) testCases.item(i);
            if (testCase.getElementsByTagName("failure").getLength() == 0
                    && testCase.getElementsByTagName("error").getLength() == 0
                    && testCase.getElementsByTagName("skipped").getLength() == 0) {
                passingTests++;
            }
        }
        Assert.assertEquals(525, passingTests);

        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: Web Scenarios/"
                + "Scenario: SOA-18974/Test 7: Visually Compare Window 2");
        Assert.assertEquals("Visual difference found at (0, 0) of size 1,920 x 870.", failure.getAttribute("message"));
        Assert.assertEquals("Source: SOA-18974_ScreenshotOutputs_test4_window2.png", failure.getTextContent());
    }

    @Test
    public void testNativeReportsProduceSchemaValidXunit()
    {
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatest_visual_difference.xml", "native-original-visual-schema-output.xml");
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatest_parabank_change_impact_with_all_functional_tests.xml", "native-change-schema-output.xml");
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatest_parabank_change_impact_without_functional_tests.xml", "native-change-only-schema-output.xml");
        soatestXunitTransformation(TEST_RESOURCES + "xml/soatest_parabank_change_impact_with_one_functional_test.xml", "native-change-filtered-schema-output.xml");
    }

    @Test
    public void testNativeChangeImpactOnlyRunPreservesAnalyzedTests() throws Exception
    {
        Document output = transform("soatest_parabank_change_impact_without_functional_tests.xml", "native-change-only-output.xml");
        // All seven change-impact checks ran even though every functional total is zero.
        assertCounts(output, 1, 7, 7);
        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: CartServicePort/Test 3: getItemById");
        Assert.assertTrue(failure.getTextContent().contains("getItemByIdentifier"));
        Assert.assertFalse(failure.getTextContent().contains("SOAP Fault"));
    }

    @Test
    public void testNativeFilteredFunctionalRunRetainsAllExecutedChangeChecks() throws Exception
    {
        Document output = transform("soatest_parabank_change_impact_with_one_functional_test.xml", "native-change-filtered-output.xml");
        // The functional filter selects submitOrder; Change Advisor still analyzes all seven clients.
        assertCounts(output, 1, 7, 7);
        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: CartServicePort/Test 1: addItemToCart");
        Assert.assertTrue(failure.getTextContent().contains("Operation \"addItemToCart\" - targetObjectUri changed"));
        Assert.assertFalse(failure.getTextContent().contains("SOAP Fault"));
    }

    @Test
    public void testNativeChangeImpactsIncludeFunctionallyPassingTestsInFailureTotals() throws Exception
    {
        Document output = transform("soatest_parabank_change_impact_with_all_functional_tests.xml", "native-change-output.xml");
        // Native functional counters exclude the passing SOAP call whose Change Advisor checks failed.
        assertCounts(output, 1, 7, 7);
        String scenario = "Test Suite: Test Suite/Test Suite: CartServicePort/";
        Element changeOnly = failure(output, scenario + "Test 6: submitOrder");
        Assert.assertEquals("Multiple errors reported", changeOnly.getAttribute("message"));
        Assert.assertEquals("Operation \"submitOrder\" - targetObjectUri changed from \"http://bookstore.parasoft.com/\""
                + " to \"http://bookstore2.parasoft.com/\"\n" + CHANGE_DETAILS
                + "\n\nElement \"submitOrder\" - namespace changed from \"http://bookstore.parasoft.com/\""
                + " to \"http://bookstore2.parasoft.com/\"\n" + CHANGE_DETAILS, changeOnly.getTextContent());

        Element mixed = failure(output, scenario + "Test 1: addItemToCart");
        Assert.assertTrue(mixed.getTextContent().contains("Operation \"addItemToCart\" - targetObjectUri changed"));
        Assert.assertTrue(mixed.getTextContent().contains("Element \"addItemToCart\" - namespace changed"));
        Assert.assertTrue(mixed.getTextContent().contains("SOAP Fault"));
        Assert.assertFalse(mixed.getTextContent().contains("Operation \"getItemByTitle\""));
    }

    private Document transformExecutionViolations(String outputFileName) throws Exception
    {
        return transform(EXECUTION_VIOLATIONS_REPORT, outputFileName);
    }

    private Document transform(String fileName, String outputFileName) throws Exception
    {
        File output = XUnitTransformer.transform(TEST_RESOURCES + "xml/" + fileName,
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
        Assert.assertEquals(type, type + " details", failure.getTextContent());
    }

    private void assertFailure(Document output, String testName, String message, String details) throws Exception
    {
        NodeList failures = findTestCase(output, "Execution violations/" + testName).getElementsByTagName("failure");
        Assert.assertEquals(testName, 1, failures.getLength());
        Element failure = (Element) failures.item(0);
        Assert.assertEquals(testName, message, failure.getAttribute("message"));
        Assert.assertEquals(testName, details, failure.getTextContent());
    }

    private Element failure(Document output, String name) throws Exception
    {
        NodeList failures = findTestCase(output, name).getElementsByTagName("failure");
        Assert.assertEquals(name, 1, failures.getLength());
        return (Element) failures.item(0);
    }

    private void assertCounts(Document output, int suites, int tests, int failures)
    {
        NodeList testSuites = output.getElementsByTagName("testsuite");
        Assert.assertEquals(suites, testSuites.getLength());
        // The original browser report spans many suites; verify each summary against its emitted cases.
        for (int i = 0; i < testSuites.getLength(); i++) {
            Element suite = (Element) testSuites.item(i);
            Assert.assertEquals(suite.getAttribute("name"),
                    Integer.toString(suite.getElementsByTagName("testcase").getLength()), suite.getAttribute("tests"));
            Assert.assertEquals(suite.getAttribute("name"),
                    Integer.toString(suite.getElementsByTagName("failure").getLength()), suite.getAttribute("failures"));
        }
        Assert.assertEquals(tests, output.getElementsByTagName("testcase").getLength());
        Assert.assertEquals(failures, output.getElementsByTagName("failure").getLength());
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
