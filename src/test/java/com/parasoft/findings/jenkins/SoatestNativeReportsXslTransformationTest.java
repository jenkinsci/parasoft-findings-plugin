/*
 * Copyright 2026 Parasoft Corporation
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

public class SoatestNativeReportsXslTransformationTest
{
    private static final String XSL = "src/main/resources/com/parasoft/findings/jenkins/xunit/soatest-xunit.xsl";
    private static final String REPORTS = "src/test/resources/xml/";
    private static final String CHANGE_DETAILS = "Service Details (from the Service Catalog):\n"
            + "\tService Name:  http://localhost:18080/parabank/services/store-01V2?wsdl\n"
            + "\tSaved Service Version:  Old Version\n\tCurrent Service Version:  Current Version\n\n"
            + "Source: http://localhost:18081/current.wsdl";

    @Test
    public void testOriginalVisualDifferenceReport() throws Exception
    {
        Document output = transform("soatest-visual-difference.xml", "native-original-visual-output.xml");
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
        XUnitTransformer.testXUnitTransformation(REPORTS + "soatest-visual-difference.xml", "native-original-visual-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "soatest-parabank-change-impact-with-all-functional-tests.xml", "native-change-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "soatest-parabank-change-impact-without-functional-tests.xml", "native-change-only-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "soatest-parabank-change-impact-with-one-functional-test.xml", "native-change-filtered-schema-output.xml", XSL);
    }

    @Test
    public void testNativeChangeImpactOnlyRunPreservesAnalyzedTests() throws Exception
    {
        Document output = transform("soatest-parabank-change-impact-without-functional-tests.xml", "native-change-only-output.xml");
        // All seven change-impact checks ran even though every functional total is zero.
        assertCounts(output, 1, 7, 7);
        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: CartServicePort/Test 3: getItemById");
        Assert.assertTrue(failure.getTextContent().contains("getItemByIdentifier"));
        Assert.assertFalse(failure.getTextContent().contains("SOAP Fault"));
    }

    @Test
    public void testNativeFilteredFunctionalRunRetainsAllExecutedChangeChecks() throws Exception
    {
        Document output = transform("soatest-parabank-change-impact-with-one-functional-test.xml", "native-change-filtered-output.xml");
        // The functional filter selects submitOrder; Change Advisor still analyzes all seven clients.
        assertCounts(output, 1, 7, 7);
        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: CartServicePort/Test 1: addItemToCart");
        Assert.assertTrue(failure.getTextContent().contains("Operation \"addItemToCart\" - targetObjectUri changed"));
        Assert.assertFalse(failure.getTextContent().contains("SOAP Fault"));
    }

    @Test
    public void testNativeChangeImpactsIncludeFunctionallyPassingTestsInFailureTotals() throws Exception
    {
        Document output = transform("soatest-parabank-change-impact-with-all-functional-tests.xml", "native-change-output.xml");
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

    private Document transform(String report, String outputName) throws Exception
    {
        File output = XUnitTransformer.transform(REPORTS + report, outputName, XSL);
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(output);
    }

    private Element find(Document output, String name) throws Exception
    {
        Element testCase = (Element) XPathFactory.newInstance().newXPath()
                .evaluate("//testcase[@name='" + name + "']", output, XPathConstants.NODE);
        Assert.assertNotNull("Missing test case: " + name, testCase);
        return testCase;
    }

    private Element failure(Document output, String name) throws Exception
    {
        NodeList failures = find(output, name).getElementsByTagName("failure");
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
}
