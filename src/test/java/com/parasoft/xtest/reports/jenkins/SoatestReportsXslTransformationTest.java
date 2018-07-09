package com.parasoft.xtest.reports.jenkins;

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

    /**
     * soatestDesktopCalc3.xml has complex structure, test suites contains tests and other test suites
     */
    @Test
    public void testSoatest9DesktopCalc3FunctionalIterationsXUnitTransform()
    {
        soatestXunitTransformation("xml/soatestDesktopCalc3.xml", "soatestDesktopCalc3-output.xml");
    }

    @Test
    public void testSoatestWarFunctionalXUnitTransform()
    {
        soatestXunitTransformation("xml/soatestWar.xml", "soatestWar-output.xml");
    }

    @Test
    public void testSoatestWarLoopFunctionalXUnitTransform()
    {
        soatestXunitTransformation("xml/soatestWarLoop.xml", "soatestWarLoop-output.xml");
    }

    @Test
    public void testSoatestWarCalcDirsFunctionalXUnitTransform()
    {
        soatestXunitTransformation("xml/szmyruReport.xml", "szmyruReport-output.xml");
    }

    @Test
    public void testSoatestWar3FunctionalXUnitTransform()
    {
        soatestXunitTransformation("xml/soatestWar3.xml", "soatestWar3-output.xml");
    }

    private static void soatestXunitTransformation(String reportFileName, String outputFileName) {
        XUnitTransformer.testXUnitTransform(reportFileName, outputFileName, SOATEST_XUNIT_XSL);
    }
}