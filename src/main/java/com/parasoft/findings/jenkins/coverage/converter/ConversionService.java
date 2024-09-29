/*
 * Copyright 2023 Parasoft Corporation
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

package com.parasoft.findings.jenkins.coverage.converter;

import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Map;

// Adapted from: https://github.com/jenkinsci/libdtkit/blob/dtkit-frmk-3.0.0/dtkit-metrics-util/src/main/java/org/jenkinsci/lib/dtkit/util/converter/ConversionService.java
public class ConversionService implements Serializable {
    private static final long serialVersionUID = 9023541911137031601L;

    /**
     * Skip DTD Entity resolution.
     */
    public static class CoverageEntityResolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    }

    /**
     * Launches an XSLT conversion from a source to an OutputStream.
     * This methods uses the net.sf.saxon packages.
     *
     * @param xslSource the source of the xsl
     * @param inputFile the input file
     * @param outFile   the output file
     * @param params    the parameter map
     * @throws ConversionException the convert exception
     */
    public void convert(StreamSource xslSource, File inputFile, File outFile, Map<QName, XdmValue> params)
            throws ConversionException {
        try (InputStream input = new FileInputStream(inputFile)) {
            convert(xslSource, new InputSource(input), outFile, params);
        } catch (IOException e) {
            throw asConversionException(e);
        }
    }

    /**
     * Launches an XSLT conversion from a source to an OutputStream.
     * This methods uses the net.sf.saxon packages.
     *
     * @param xslSource the source of the xsl
     * @param inputFile the input file
     * @param outFile   the output file
     * @param params    the parameter map
     * @throws ConversionException the convert exception
     */
    public void convert(StreamSource xslSource, InputSource inputFile, File outFile, Map<QName, XdmValue> params)
            throws ConversionException {
        try (OutputStream os = new FileOutputStream(outFile)) {
            convert(xslSource, inputFile, os, params);
        } catch (Exception e) { // parasoft-suppress OWASP2021.A5.NCE "Reviewed"
            throw asConversionException(e);
        }
    }

    private void convert(StreamSource xslSource, InputSource inputFile, OutputStream output,
                         Map<QName, XdmValue> params) throws SaxonApiException {
        // create the conversion processor with a XSLT compiler
        Processor processor = new Processor(false);
        processor.setConfigurationProperty(Feature.ENTITY_RESOLVER_CLASS, CoverageEntityResolver.class.getName());
        processor.setConfigurationProperty(Feature.DTD_VALIDATION, false);
        processor.setConfigurationProperty(Feature.DTD_VALIDATION_RECOVERABLE, true);
        // remove DTD validation warning messages on system error
        processor.getUnderlyingConfiguration().setValidation(false);
        XsltCompiler compiler = processor.newXsltCompiler();

        // compile and load the XSL file
        XsltExecutable stylesheet = compiler.compile(xslSource);
        Xslt30Transformer transformer = stylesheet.load30();
        transformer.setStylesheetParameters(params);

        // create the output with its options
        Serializer out = processor.newSerializer(output);
        out.setOutputProperty(Serializer.Property.INDENT, "yes");

        // unwrap input stream to maintain APIs back compatible
        Source source = new StreamSource(inputFile.getByteStream());
        // run the conversion
        transformer.transform(source, out);
    }

    private ConversionException asConversionException(Exception e) {
        if (e instanceof FileNotFoundException) {
            return new ConversionException(e);
        } else if (e instanceof IOException) {
            return new ConversionException("Conversion Error", e);
        } else if (e instanceof SaxonApiException) {
            return new ConversionException("Error to convert the input XML document", e);
        } else if (e instanceof SAXException || e instanceof ParserConfigurationException) {
            // TODO verify that this kind of message agree with the exception reason
            return new ConversionException("Error to convert - A file not found", e);
        } else {
            return new ConversionException(e);
        }
    }
}
