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

import static org.junit.Assert.fail;

import com.icl.saxon.TransformerFactoryImpl;
import com.parasoft.xtest.common.UIO;
import com.parasoft.xtest.common.io.FileUtil;
import com.parasoft.xtest.reports.jenkins.xunit.ParasoftInputMetric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XSLTransformTest {

    /**
     * @task 66687
     */
    @Test
    public void testXUnitTransform() {
        testXUnitTransform("xml/jTest_10_unit.xml", "junit-jtest.xml");
        testXUnitTransform("xml/simulator_unit.xml", "junit-sim.xml");
    }

    /**
     * @task 88025
     */
    @Test
    public void testDemoXUnitTransform() {
        try {
            URL report = XSLTransformTest.class.getResource("xml/jTest_10.2_unit.xml");
            ParasoftInputMetric metric = new ParasoftInputMetric();
            URL resource = ParasoftInputMetric.class.getResource(metric.getXslName());
            File outputFile = new File("junit-demo.xml");
            outputFile.deleteOnExit();
            transform(report, resource, outputFile);
            TagCounterVerifier verifier = new TagCounterVerifier();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(outputFile, verifier);

            Assert.assertEquals(4, verifier.getNumber("failure"));
            Assert.assertEquals(4, verifier.getNumber("error"));

        } catch (Exception e) {
            doFail(e);
        }
    }
    
    @Test
    public void testCppTestUnitXUnitTransform() {
        try {
            URL report = XSLTransformTest.class.getResource("xml/cppTest_10.3.3_unit.xml");
            ParasoftInputMetric metric = new ParasoftInputMetric();
            URL resource = ParasoftInputMetric.class.getResource(metric.getXslName());
            File outputFile = new File("junit-demo.xml");
            outputFile.deleteOnExit();
            transform(report, resource, outputFile);
            TagCounterVerifier verifier = new TagCounterVerifier();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(outputFile, verifier);

            Assert.assertEquals(147, verifier.getNumber("failure"));
            Assert.assertEquals(0, verifier.getNumber("error"));

        } catch (Exception e) {
            doFail(e);
        }
    }
    
    @Test
    public void testCppTesEnginetUnitXUnitTransform() {
        try {
            URL report = XSLTransformTest.class.getResource("xml/cppTest_10.3.4_Engine_unit.xml");
            ParasoftInputMetric metric = new ParasoftInputMetric();
            URL resource = ParasoftInputMetric.class.getResource(metric.getXslName());
            File outputFile = new File("junit-demo.xml");
            outputFile.deleteOnExit();
            transform(report, resource, outputFile);
            TagCounterVerifier verifier = new TagCounterVerifier();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(outputFile, verifier);

            Assert.assertEquals(12, verifier.getNumber("failure"));
            Assert.assertEquals(5, verifier.getNumber("error"));

        } catch (Exception e) {
            doFail(e);
        }
    }

    private static void doFail(Exception e) {
        fail(e.getClass().getName() + ": " + e.getMessage());
    }

    private void testXUnitTransform(String reportFileName, String outputFileName) {
        try {
            URL report = XSLTransformTest.class.getResource(reportFileName);
            ParasoftInputMetric metric = new ParasoftInputMetric();
            URL resource = ParasoftInputMetric.class.getResource( metric.getXslName());
            File outputFile = new File(outputFileName);
            outputFile.deleteOnExit();

            transform(report, resource, outputFile);
            printContents(outputFile);
            validate(outputFile);

        } catch (TransformerException e) {
            Logger.getLogger().error(e);
            doFail(e);
        } catch (IOException e) {
            Logger.getLogger().error(e);
            doFail(e);
        } catch (SAXException e) {
            Logger.getLogger().error(e);
            doFail(e);
        }
    }

    private static void validate(File outputFile) throws SAXException, IOException {
        URL schemaFile = XSLTransformTest.class.getResource("xml/junit-7.xsd");
        Source xmlFile = new StreamSource(outputFile);
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    private static void printContents(File outputFile) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        FileUtil.readFile(outputFile, lines);

        for (String line : lines) {
            System.out.println(line);
        }
    }

    private static void transform(URL inputUrl, URL xslURL, File outputFile) throws TransformerException {
        InputStream inputStream = null;
        InputStream xslStream = null;
        try {
            inputStream = inputUrl.openStream();
            Source xmlInput = new StreamSource(inputStream);
            xslStream = xslURL.openStream();
            Source xsl = new StreamSource(xslStream);
            Result xmlOutput = new StreamResult(outputFile);
            Transformer newTransformer = TransformerFactoryImpl.newInstance().newTransformer(xsl);
            newTransformer.transform(xmlInput, xmlOutput);
        } catch (IOException ex) {
            Logger.getLogger().error(ex);
            doFail(ex);
        } finally {
            UIO.close(inputStream);
            UIO.close(xslStream);
        }

    }

    public class TagCounterVerifier extends DefaultHandler {
        private final Map<String, Integer> _tagCountMap = new HashMap<String, Integer>();

        public TagCounterVerifier() {}

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            Integer integer = _tagCountMap.get(qName);
            if (integer == null) {
                integer = 0;
            }
            _tagCountMap.put(qName, integer + 1);
        }

        public int getNumber(String sName) {
            return _tagCountMap.get(sName) == null ? 0 : _tagCountMap.get(sName);
        }
    }

}
