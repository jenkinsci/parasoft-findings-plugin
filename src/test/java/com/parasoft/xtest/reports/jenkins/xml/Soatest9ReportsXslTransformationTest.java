package com.parasoft.xtest.reports.jenkins.xml;

import com.parasoft.xtest.reports.jenkins.XUnitTransformer;
import org.junit.Test;

public class Soatest9ReportsXslTransformationTest
{

    private static final String SOATEST_XUNIT_XSL = "src/main/resources/com/parasoft/xtest/reports/jenkins/xunit/soatest-xunit.xsl";

    @Test
    public void testSoatest9DesktopFunctionalXUnitTransform()
    {
        soatestXunitTransformation("xml/soatestDesktop9Functional.xml", "soatestDesktop9Functional-output.xml");
    }

    @Test
    public void testSoatest9DesktopFunctionalIterationsXUnitTransform()
    {
        soatestXunitTransformation("xml/soatestDesktop9FunctionalIterations.xml", "soatestDesktop9FunctionalIterations-output.xml");
    }

    private static void soatestXunitTransformation(String reportFileName, String outputFileName) {
        XUnitTransformer.testXUnitTransform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);
    }
}