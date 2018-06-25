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
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class XSLTransformTest
{

    private static final String XUNIT_XSL = "src/main/resources/com/parasoft/xtest/reports/jenkins/xunit/xunit.xsl";

    /**
     * @task 66687
     */
    @Test
    public void testXUnitTransform()
    {
        xunitTransformation("xml/jTest_10_unit.xml", "junit-jtest.xml");
        xunitTransformation("xml/simulator_unit.xml", "junit-sim.xml");
    }

    @Test
    public void testCppTest10EngineXUnitTransform2()
    {
        xunitTransformation("xml/c++Test10Engine.xml", "c++Test10Engine-output.xml");
    }

    @Test
    public void testCppTest10EngineXUnitTransform()
    {
        xunitTransformation("xml/c++Test10DesktopVisualStudio.xml", "c++Test10DesktopVisualStudio-output.xml");
    }

    /**
     * @task 88025
     */
    @Test
    public void testDemoXUnitTransform()
    {
        try {
            URL report = XSLTransformTest.class.getResource("xml/jTest_10.2_unit.xml");
            URL resource = new File(XUNIT_XSL).toURI().toURL();
            File outputFile = new File("junit-demo.xml");
            outputFile.deleteOnExit();
            XUnitTransformer.transform(report, resource, outputFile);
            TagCounterVerifier verifier = new TagCounterVerifier();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(outputFile, verifier);

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
            URL report = XSLTransformTest.class.getResource("xml/cppTest_10.3.3_unit.xml");
            URL resource = new File(XUNIT_XSL).toURI().toURL();
            File outputFile = new File("junit-demo.xml");
            outputFile.deleteOnExit();
            XUnitTransformer.transform(report, resource, outputFile);
            TagCounterVerifier verifier = new TagCounterVerifier();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(outputFile, verifier);

            Assert.assertEquals(147, verifier.getNumber("failure"));
            Assert.assertEquals(0, verifier.getNumber("error"));

        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    @Test
    public void testCppTesEnginetUnitXUnitTransform()
    {
        try {
            URL report = XSLTransformTest.class.getResource("xml/cppTest_10.3.4_Engine_unit.xml");
            URL resource = new File(XUNIT_XSL).toURI().toURL();
            File outputFile = new File("junit-demo.xml");
            outputFile.deleteOnExit();
            XUnitTransformer.transform(report, resource, outputFile);
            TagCounterVerifier verifier = new TagCounterVerifier();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(outputFile, verifier);

            Assert.assertEquals(12, verifier.getNumber("failure"));
            Assert.assertEquals(5, verifier.getNumber("error"));

        } catch (Exception e) {
            XUnitTransformer.doFail(e);
        }
    }

    private static void xunitTransformation(String reportFileName, String outputFileName)
    {
        XUnitTransformer.testXUnitTransform(reportFileName, outputFileName, XUNIT_XSL);
    }

    public class TagCounterVerifier
        extends DefaultHandler
    {
        private final Map<String, Integer> _tagCountMap = new HashMap<>();

        TagCounterVerifier()
        {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
        {
            Integer integer = _tagCountMap.get(qName);
            if (integer == null) {
                integer = 0;
            }
            _tagCountMap.put(qName, integer + 1);
        }

        int getNumber(String sName)
        {
            return _tagCountMap.get(sName) == null ? 0 : _tagCountMap.get(sName);
        }
    }
}
