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
    private static final String REPORTS = "src/test/resources/xml/soatest-native/";
    private static final String SINGLE = "Scenario: CICD1179_ParaBank_Single/";
    private static final String PARAMETERIZED = "Scenario: CICD1179_ParaBank_Parameterized/";
    private static final String CHANGE_DETAILS = "Service Details (from the Service Catalog):\n"
            + "\tService Name:  http://localhost:18080/parabank/services/store-01V2?wsdl\n"
            + "\tSaved Service Version:  Old Version\n\tCurrent Service Version:  Current Version\n\n"
            + "Source: http://localhost:18081/current.wsdl";

    @Test
    public void testNativeSingleCaseStatusAndConnectionDiagnostics() throws Exception
    {
        Document output = transform("parabank-single.xml", "native-single-output.xml");
        assertCounts(output, 4, 3);
        Assert.assertEquals(0, find(output, SINGLE + "Test 1: Customer passes").getElementsByTagName("failure").getLength());
        Assert.assertEquals("0.356", find(output, SINGLE + "Test 1: Customer passes").getAttribute("time"));

        Element status = failure(output, SINGLE + "Test 2: Unexpected status");
        Assert.assertEquals("Response code Received HTTP Response Code 200 is not in valid range (Valid range: 201): 200 ",
                status.getAttribute("message"));
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><customer><id>12212</id>"
                + "<firstName>John</firstName><lastName>Smith</lastName><address><street>1431 Main St</street>"
                + "<city>Beverly Hills</city><state>CA</state><zipCode>90210</zipCode></address>"
                + "<phoneNumber>310-447-4121</phoneNumber><ssn>622-11-9999</ssn></customer>", status.getTextContent());

        // SOAtest emits a fatal FuncViol for this transport failure, not a UnitViol or RuntimeViol.
        Element connection = failure(output, SINGLE + "Test 4: Refused connection");
        Assert.assertEquals("Connection refused: connect", connection.getAttribute("message"));
        Assert.assertEquals("A connection error has been detected.  This may occur if a test has timed out"
                + " while the client was still attempting to send or receive data.", connection.getTextContent());
    }

    @Test
    public void testNativeMultipleViolationsRemainOneFailure() throws Exception
    {
        Document output = transform("parabank-single.xml", "native-multiple-output.xml");
        Element failure = failure(output, SINGLE + "Test 3: Multiple value mismatches");
        Assert.assertEquals("Multiple errors reported", failure.getAttribute("message"));
        Assert.assertEquals("Wrong first name: For element \"../firstName\", expected: Jane but was: John"
                + "\nFor element \"../firstName\", expected: Jane but was: John"
                + "\n\nWrong last name: For element \"../lastName\", expected: Doe but was: Smith"
                + "\nFor element \"../lastName\", expected: Doe but was: Smith", failure.getTextContent());
    }

    @Test
    public void testNativeParameterizedFailuresStayWithTheirTestAndRow() throws Exception
    {
        Document output = transform("parabank-parameterized.xml", "native-parameterized-output.xml");
        assertCounts(output, 4, 1);
        Assert.assertEquals(0, find(output, PARAMETERIZED + "Test 1: Customer row assertions[Customers (row 1)]")
                .getElementsByTagName("failure").getLength());
        Assert.assertEquals(0, find(output, PARAMETERIZED + "Test 2: Passing row control[Customers (row 1)]")
                .getElementsByTagName("failure").getLength());
        // The control shares tcId with the failing row; matching testId as well prevents leakage.
        Assert.assertEquals(0, find(output, PARAMETERIZED + "Test 2: Passing row control[Customers (row 2)]")
                .getElementsByTagName("failure").getLength());
        Element failure = failure(output, PARAMETERIZED + "Test 1: Customer row assertions[Customers (row 2)]");
        Assert.assertEquals("Multiple errors reported", failure.getAttribute("message"));
        Assert.assertEquals("DataSource: Customers (row 2): Customer ID from row: For element \"../id\", expected: 12213 but was: 12212"
                + "\nFor element \"../id\", expected: 12213 but was: 12212"
                + "\n\nDataSource: Customers (row 2): First name from row: For element \"../firstName\", expected: Jane but was: John"
                + "\nFor element \"../firstName\", expected: Jane but was: John", failure.getTextContent());
        Assert.assertEquals("0.021", find(output, PARAMETERIZED + "Test 1: Customer row assertions[Customers (row 2)]")
                .getAttribute("time"));
    }

    @Test
    public void testNativeReportsProduceSchemaValidXunit()
    {
        XUnitTransformer.testXUnitTransformation(REPORTS + "parabank-single.xml", "native-single-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "parabank-parameterized.xml", "native-parameterized-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "parabank-visual.xml", "native-visual-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "java-exceptions.xml", "native-exceptions-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "parabank-change-impact.xml", "native-change-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "parabank-change-impact-only.xml", "native-change-only-schema-output.xml", XSL);
        XUnitTransformer.testXUnitTransformation(REPORTS + "parabank-change-impact-filtered.xml", "native-change-filtered-schema-output.xml", XSL);
    }

    @Test
    public void testNativeChangeImpactOnlyRunPreservesAnalyzedTests() throws Exception
    {
        Document output = transform("parabank-change-impact-only.xml", "native-change-only-output.xml");
        // All seven change-impact checks ran even though every functional total is zero.
        assertCounts(output, 7, 7);
        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: CartServicePort/Test 3: getItemById");
        Assert.assertTrue(failure.getTextContent().contains("getItemByIdentifier"));
        Assert.assertFalse(failure.getTextContent().contains("SOAP Fault"));
    }

    @Test
    public void testNativeFilteredFunctionalRunRetainsAllExecutedChangeChecks() throws Exception
    {
        Document output = transform("parabank-change-impact-filtered.xml", "native-change-filtered-output.xml");
        // The functional filter selects submitOrder; Change Advisor still analyzes all seven clients.
        assertCounts(output, 7, 7);
        Element failure = failure(output, "Test Suite: Test Suite/Test Suite: CartServicePort/Test 1: addItemToCart");
        Assert.assertTrue(failure.getTextContent().contains("Operation \"addItemToCart\" - targetObjectUri changed"));
        Assert.assertFalse(failure.getTextContent().contains("SOAP Fault"));
    }

    @Test
    public void testNativeChangeImpactsIncludeFunctionallyPassingTestsInFailureTotals() throws Exception
    {
        Document output = transform("parabank-change-impact.xml", "native-change-output.xml");
        // Native functional counters exclude the passing SOAP call whose Change Advisor checks failed.
        assertCounts(output, 7, 7);
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

    @Test
    public void testNativeVisualDifferenceIsReportedAsFailure() throws Exception
    {
        Document output = transform("parabank-visual.xml", "native-visual-output.xml");
        assertCounts(output, 2, 1);
        String scenario = "Scenario: CICD1179_ParaBank_Visual/";
        Assert.assertEquals(0, find(output, scenario + "Test 1: ParaBank unchanged image")
                .getElementsByTagName("failure").getLength());
        Element failure = failure(output, scenario + "Test 2: ParaBank changed image");
        Assert.assertEquals("Visual difference found at (1, 1) of size 30 x 30.", failure.getAttribute("message"));
        Assert.assertEquals("Source: text", failure.getTextContent());
        Assert.assertEquals("0.020", find(output, scenario + "Test 2: ParaBank changed image").getAttribute("time"));
    }

    @Test
    public void testNativeJavaAndJunitErrorsPreserveFunctionalDiagnostics() throws Exception
    {
        Document output = transform("java-exceptions.xml", "native-exceptions-output.xml");
        assertCounts(output, 4, 3);
        String scenario = "Scenario: CICD1179_Java_Exceptions/";
        String prefix = "Error during script execution.  View Details for more information.\r\n";
        Element assertion = failure(output, scenario + "Test 1: Java assertion");
        Assert.assertEquals(prefix + "Intentional Java assertion from SOAtest extension", assertion.getAttribute("message"));
        Assert.assertTrue(assertion.getTextContent().contains("java.lang.AssertionError: Intentional Java assertion from SOAtest extension"));

        // These real Java failures remain FuncViol; their stack traces are stored in violationDetails.
        Element runtime = failure(output, scenario + "Test 2: Java runtime exception");
        Assert.assertEquals(prefix + "Inner cause", runtime.getAttribute("message"));
        String details = runtime.getTextContent();
        int outer = details.indexOf("java.lang.IllegalStateException: Outer exception");
        int cause = details.indexOf("Caused by: java.lang.IllegalArgumentException: Inner cause");
        Assert.assertTrue(details, outer >= 0 && cause > outer);

        Element junit = failure(output, scenario + "Test 3: Failing JUnit invocation");
        Assert.assertEquals(prefix + "Intentional JUnit mismatch expected:<1> but was:<2>", junit.getAttribute("message"));
        Assert.assertTrue(junit.getTextContent().contains("java.lang.AssertionError: Intentional JUnit mismatch expected:<1> but was:<2>"));
        Assert.assertTrue(junit.getTextContent().contains("org.junit.Assert.assertEquals"));
        Assert.assertEquals(0, find(output, scenario + "Test 4: Passing Java control")
                .getElementsByTagName("failure").getLength());
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

    private void assertCounts(Document output, int tests, int failures)
    {
        Assert.assertEquals(1, output.getElementsByTagName("testsuite").getLength());
        Element suite = (Element) output.getElementsByTagName("testsuite").item(0);
        Assert.assertEquals(Integer.toString(tests), suite.getAttribute("tests"));
        Assert.assertEquals(Integer.toString(failures), suite.getAttribute("failures"));
        Assert.assertEquals(tests, output.getElementsByTagName("testcase").getLength());
        Assert.assertEquals(failures, output.getElementsByTagName("failure").getLength());
    }
}
