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

import com.parasoft.findings.utils.common.util.FileUtil;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.jenkinsci.lib.dtkit.util.converter.ConversionService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.fail;

class XUnitTransformer
{
    static void testXUnitTransformation(String reportFileName, String outputFileName, String pathToXslSchema)
    {
        try {
            File outputFile = transform(reportFileName, outputFileName, pathToXslSchema);

            printContents(outputFile);
            validateAgainstXslSchemas(outputFile);
        } catch (ConversionException | SAXException | IOException e) {
            Logger.getLogger().error(e);
            doFail(e);
        }
    }

    static File transform(String reportFileName, String outputFileName, String pathToXslSchema)
            throws MalformedURLException, ConversionException
    {
        URL report = new File(reportFileName).toURI().toURL();
        URL resource = new File(pathToXslSchema).toURI().toURL();
        File outputFile = new File(outputFileName);
        outputFile.deleteOnExit();
        transform(report, resource, outputFile);
        return outputFile;
    }

    private static void printContents(File outputFile)
            throws IOException
    {
        ArrayList<String> lines = new ArrayList<>();
        FileUtil.readFile(outputFile, lines);

        for (String line: lines) {
            System.out.println(line);
        }
    }

    private static void validateAgainstXslSchemas(File outputFile)
            throws SAXException, IOException
    {
        // keep compatibility with old schema
        validate(outputFile, "src/test/resources/schema/junit-7.xsd");
        // validate against Ant Junit schema: https://github.com/windyroad/JUnit-Schema/blob/master/JUnit.xsd
        validate(outputFile, "src/test/resources/schema/antJunitSchema.xsd");
    }

    private static void validate(File outputFile, String schemaPath)
            throws SAXException, IOException
    {
        URL schemaFile = new File(schemaPath).toURI().toURL();
        Source xmlFile = new StreamSource(outputFile);
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    static void transform(URL inputUrl, URL xslURL, File outputFile)
            throws ConversionException
    {
        InputStream inputStream = null;
        InputStream xslStream = null;
        try {
            inputStream = inputUrl.openStream();
            InputSource inputSource = new InputSource(inputStream);
            xslStream = xslURL.openStream();
            StreamSource xslSource = new StreamSource(xslStream);
            ConversionService conversionService = new ConversionService();
            conversionService.convert(xslSource, inputSource, outputFile, null);
        } catch (IOException ex) {
            Logger.getLogger().error(ex);
            doFail(ex);
        } finally {
            close(inputStream);
            close(xslStream);
        }
    }

    static void parseXunitOutputXml(File outputFile, TagCounterVerifier verifier)
            throws ParserConfigurationException, SAXException, IOException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(outputFile, verifier);
    }

    static void doFail(Exception e)
    {
        fail(e.getClass().getName() + ": " + e.getMessage());
    }

    /**
     * Safe closing.
     *
     * @param stream
     */
    public static void close(InputStream stream)
    {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger().error(ex);
        }
    }
}