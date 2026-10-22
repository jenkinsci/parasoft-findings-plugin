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
