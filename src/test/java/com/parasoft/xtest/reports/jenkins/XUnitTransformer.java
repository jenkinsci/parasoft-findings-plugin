package com.parasoft.xtest.reports.jenkins;

import com.icl.saxon.TransformerFactoryImpl;
import com.parasoft.xtest.common.UIO;
import com.parasoft.xtest.common.io.FileUtil;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.fail;

public class XUnitTransformer
{
    public static void testXUnitTransform(String reportFileName, String outputFileName, String pathToXslSchema)
    {
        try {
            URL report = XSLTransformTest.class.getResource(reportFileName);
            URL resource = new File(pathToXslSchema).toURI().toURL();
            File outputFile = new File(outputFileName);
            outputFile.deleteOnExit();

            transform(report, resource, outputFile);
            printContents(outputFile);
            validateAgainstXslSchemas(outputFile);

        } catch (TransformerException | SAXException | IOException e) {
            Logger.getLogger().error(e);
            doFail(e);
        }
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
        validate(outputFile, "xml/junit-7.xsd");
        // validate against Ant Junit schema: https://github.com/windyroad/JUnit-Schema/blob/master/JUnit.xsd
        validate(outputFile, "xml/antJunitSchema.xsd");
    }

    private static void validate(File outputFile, String schemaPath)
        throws SAXException, IOException
    {
        URL schemaFile = XSLTransformTest.class.getResource(schemaPath);
        Source xmlFile = new StreamSource(outputFile);
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    static void transform(URL inputUrl, URL xslURL, File outputFile)
        throws TransformerException
    {
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

    static void doFail(Exception e)
    {
        fail(e.getClass().getName() + ": " + e.getMessage());
    }
}