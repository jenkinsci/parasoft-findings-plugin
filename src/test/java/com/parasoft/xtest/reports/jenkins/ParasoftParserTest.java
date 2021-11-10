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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hamcrest.collection.IsIn;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.parasoft.xtest.common.api.IProjectFileTestableInput;
import com.parasoft.xtest.common.api.ISourceRange;
import com.parasoft.xtest.reports.jenkins.parser.DupIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.FlowIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftIssueAdditionalProperties;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement.Type;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IResultLocation;
import com.parasoft.xtest.results.api.IRuleViolation;
import com.parasoft.xtest.results.api.IViolation;
import com.parasoft.xtest.results.api.attributes.IRuleAttributes;
import com.parasoft.xtest.results.api.importer.IRulesImportHandler;

import edu.hm.hafner.analysis.FileReaderFactory;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

/**
 * Tests the extraction of Parasoft analysis results.
 */
@SuppressWarnings("nls")
public class ParasoftParserTest
{

    private static final String TEST_RESOURCES = "src/test/resources/";

    private static final String PROJECT_NAME = "com.parasoft.xtest.reports.jenkins";

    private static final String PROJECT_PATH = "E:\\Eclipse Workspace\\com.parasoft.xtest.reports.jenkins";

    private static final String RELATIVE_PATH = "\\src\\main\\java\\com\\parasoft\\xtest\\reports\\jenkins";

    private static ParasoftParser _parser = null;

    @BeforeClass
    public static void initialize()
    {
        _parser = new ParasoftParser(new Properties(), "workspace");
    }

    @AfterClass
    public static void finish()
    {}

    /**
     * @Task 23895
     * @PR 107961
     */
    @Test
    public void parseStdViolReportTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jTest_10_static_2.xml");
        assertEquals(503, report.getSize());
    }

    @Test
    public void parseFAViolReportTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jTest_10_static.xml");
        assertEquals(65, report.getSize());
    }

    @Test
    public void parseStdViolReportJtest_10_5_2Test()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jtest_10.5.2_static.xml");
        assertEquals(808, report.getSize());
    }

    @Test
    public void parseCppEngineTestStatic()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.4_engine_static.xml");
        assertEquals(5, report.getSize());
    }

    /**
     * @task 23266
     */
    @Test
    public void parseFAViolReportTest2()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jTest_10_static_empty_element.xml");
        assertEquals(16, report.getSize());
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof FlowIssueAdditionalProperties);
            String flowPath = ((FlowIssueAdditionalProperties) properties).getCallHierarchy(null);
            assertNotNull(flowPath);
        }
    }

    @Test
    public void parseFAViolWithAnnotationsReportTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jTest_10_static_with_annotations.xml");
        assertEquals(64, report.getSize());
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof FlowIssueAdditionalProperties);
            String flowPath = ((FlowIssueAdditionalProperties) properties).getCallHierarchy(null);
            assertNotNull(flowPath);
        }
    }

    @Test
    public void parseDCViolReportTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jTest_10_static_code_dup.xml");
        assertEquals(2, report.getSize());
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof DupIssueAdditionalProperties);
            String duplicates = ((DupIssueAdditionalProperties) properties).getCallHierarchy(null);
            assertNotNull(duplicates);
        }
    }

    @Test
    public void parseCppMetricsViolsTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.4.2_engine_metrics.xml");

        assertEquals(105, report.getSize());
        int countFlow = 0;
        int countMetrics = 0;
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof ParasoftIssueAdditionalProperties);
            ParasoftIssueAdditionalProperties additionalProperties = (ParasoftIssueAdditionalProperties)properties;
            if (additionalProperties instanceof FlowIssueAdditionalProperties) {
                assertEquals("com.parasoft.xtest.cpp.analyzer.static.flow", additionalProperties.getAnalyzer());   
                countFlow++;
            } else {
                if (issue.getType().startsWith("METRIC")) {
                    assertEquals("com.parasoft.xtest.cpp.analyzer.static.metrics", additionalProperties.getAnalyzer());   
                    countMetrics++;
                } else {
                    assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", additionalProperties.getAnalyzer());      
                }
            }
        }
        assertEquals(30, countFlow);
        assertEquals(64, countMetrics);
    }

    @Test
    public void parseCppDesktopStdViolsTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_static.xml");

        assertEquals(7, report.getSize());
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof ParasoftIssueAdditionalProperties);
            ParasoftIssueAdditionalProperties additionalProperties = (ParasoftIssueAdditionalProperties) properties;
            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", additionalProperties.getAnalyzer());
        }
    }

    @Test
    public void parseCppDesktopStdViolsCategoriesTest_Old()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_static.xml");

        String[] rules = { "CODSTA-39", "CODSTA-39", "CODSTA-39", "MISRA2004-15_2", "INIT-04", "INIT-04", "INIT-04" };
        assertEquals(7, report.getSize());
        int i = 0;
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof ParasoftIssueAdditionalProperties);
            ParasoftIssueAdditionalProperties additionalProperties = (ParasoftIssueAdditionalProperties) properties;

            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", additionalProperties.getAnalyzer());

            assertEquals(rules[i], issue.getType());
            assertEquals("", issue.getCategory()); // TODO - empty category should be "-"
            i++;
        }
    }

    @Test
    public void parseCppStdViolsTest_10_5()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.5.0_static.xml");
        String[] authors = { "tester", "tpieczkowski", "tpieczkowski", "tpieczkowski", "tpieczkowski", "tpieczkowski"};
        checkStaticReport(report, authors);
    }

    @Test
    public void parseCppStdViolsTest_10_5_1()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.5.1_static.xml");
        checkStaticReport(report, new String[] {"mgorecka"});
    }

    @Test
    public void parseCppStdViolsTest_10_5_2()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.5.2_static.xml");
        checkStaticReport(report, new String[] {"mgorecka"});
    }

    private void checkStaticReport(Report report, String[] authors)
    {
        String[] rules = { "INIT-06", "OPT-14", "OPT-14", "CODSTA-CPP-04", "OPT-14", "OOP-23"};
        String[] categories = { "Initialization", "Optimization", "Optimization", "Coding Conventions for C++", "Optimization", "Object Oriented"};
        assertEquals(6, report.getSize());
        int i = 0;
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof ParasoftIssueAdditionalProperties);
            ParasoftIssueAdditionalProperties additionalProperties = (ParasoftIssueAdditionalProperties)properties;

            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", additionalProperties.getAnalyzer());
            assertEquals(rules[i], issue.getType());
            assertEquals(categories[i], issue.getCategory());
            if (authors.length == 1) {
                assertEquals(authors[0], additionalProperties.getAuthor());
            } else {
                assertEquals(authors[i], additionalProperties.getAuthor());
            }
            i++;
        }
    }

    @Test
    public void parseJtestStdViolsTest_10_5()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/jTest_10.5.0_static.xml");
        assertEquals(67, report.getSize());
    }

    @Test
    public void parseCppDesktopStdViolsCategoriesTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_static_categories.xml");

        assertEquals(7, report.getSize());
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof ParasoftIssueAdditionalProperties);
            ParasoftIssueAdditionalProperties additionalProperties = (ParasoftIssueAdditionalProperties) properties;

            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", additionalProperties.getAnalyzer());
            assertThat(issue.getCategory(), IsIn.isIn(new String[] { "Coding Conventions", "Initialization", "MISRA C 2004" }));  //$NON-NLS-2$  //$NON-NLS-3$
        }
    }

    @Test
    public void parseCppDesktopFAViolsTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_flowanalysis.xml");

        assertEquals(4, report.getSize());
        checkFAReport(report, "mlyko");
    }

    @Test
    public void parseCppDesktop_10_5_1_FAViolsTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.5.1_flowanalysis.xml");

        assertEquals(2, report.getSize());
        checkFAReport(report, "mgorecka");
    }

    @Test
    public void parseCppDesktop_10_5_2_FAViolsTest()
    {
        Report report = parseFile(TEST_RESOURCES + "xml/cppTest_10.5.2_flowanalysis.xml");

        assertEquals(2, report.getSize());
        checkFAReport(report, "mgorecka");
    }

    private void checkFAReport(Report report, String author)
    {
        for (Issue issue : report) {
            Serializable properties = issue.getAdditionalProperties();
            assertTrue(properties instanceof ParasoftIssueAdditionalProperties);
            ParasoftIssueAdditionalProperties additionalProperties = (ParasoftIssueAdditionalProperties) properties;
            assertEquals("com.parasoft.xtest.cpp.analyzer.static.flow", additionalProperties.getAnalyzer());
            assertEquals(author, additionalProperties.getAuthor());
            assertEquals("unknown", additionalProperties.getRevision());
        }
    }

    private Report parseFile(String name)
    {
        return parseFile(name, _parser);
    }

    private static Report parseFile(String name, ParasoftParser parser)
    {
        URL resource = null;
        try {
            resource = new File(name).toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File file;
        try {
            file = new File(resource.toURI());
            ReaderFactory readerFactory = new FileReaderFactory(file.toPath(), StandardCharsets.UTF_8);
            return parser.parse(readerFactory);
        } catch (URISyntaxException e) {
            fail();
        }
        return null;
    }

    /**
     * @task 21606
     * @task 21831
     */
    @Test
    public void convertViolationsTest()
    {
        List<IViolation> violations = mockViolations();
        IRulesImportHandler rulesImportHandlerMock = Mockito.mock(IRulesImportHandler.class);
        Mockito.when(rulesImportHandlerMock.getCategoryDescription("INIT")).thenReturn("Initialize");
        Report report = _parser.convert(violations.iterator(), rulesImportHandlerMock);
        Iterator<Issue> issueIterator = report.iterator();
        Issue issue = issueIterator.next();

        assertEquals(PROJECT_NAME, issue.getModuleName());
        assertEquals(Severity.WARNING_LOW, issue.getSeverity());
        assertNotNull(issue.getPackageName());

        assertEquals("Initialize", issue.getCategory());
        assertEquals("ParasoftPlugin.java", issue.getBaseName());
        assertEquals("INIT.AAI", issue.getType());
        assertNotNull(issue.getDescription());

        issue = issueIterator.next();
        assertEquals(Severity.WARNING_LOW, issue.getSeverity());

        issue = issueIterator.next();
        assertEquals(Severity.WARNING_NORMAL, issue.getSeverity());

        issue = issueIterator.next();
        assertEquals(Severity.WARNING_NORMAL, issue.getSeverity());

        issue = issueIterator.next();
        assertEquals(Severity.WARNING_HIGH, issue.getSeverity());
    }

    /**
     * @task 21692
     * @task 21831
     */
    @Test
    public void convertFAViolations()
    {
        List<IViolation> violations = mockFAViolations();
        IRulesImportHandler rulesImportHandlerMock = Mockito.mock(IRulesImportHandler.class);
        Mockito.when(rulesImportHandlerMock.getCategoryDescription(Mockito.anyString())).thenReturn("Flow Analysis");
        String expectedFlowPath = "<ul><li><b></b><span style=\"color:#808080\">ParasoftResult.java:5</span>&nbsp&nbsp<code><span style=\"color:#808080\">Element Description</span></code></li><li><b></b><span style=\"color:#808080\">ParasoftPublisher.java:6</span>&nbsp&nbsp<code><span style=\"color:#808080\">Element Description</span></code></li><li><b></b><span style=\"color:#808080\">ParasoftHealthDescriptor.java:7</span>&nbsp&nbsp<code><span style=\"color:#808080\">Element Description</span></code><ul><li><b></b><span style=\"color:#808080\">ParasoftDetailBuilder.java:5</span>&nbsp&nbsp<code><span style=\"color:#808080\">Element Description</span></code></li></ul><ul><li><b></b><span style=\"color:#808080\">ParasoftProjectAction.java:6</span>&nbsp&nbsp<code><span style=\"color:#808080\">Element Description</span></code></li></ul></li></ul>";
        Report report = _parser.convert(violations.iterator(), rulesImportHandlerMock);
        Iterator<Issue> iterator = report.iterator();

        Issue issue = iterator.next();
        assertEquals(PROJECT_NAME, issue.getModuleName());
        assertEquals(Severity.WARNING_HIGH, issue.getSeverity());
        assertNotNull(issue.getPackageName());
        assertEquals("Flow Analysis", issue.getCategory());
        assertEquals("ParasoftPlugin.java", issue.getBaseName());
        assertEquals("FA.FA", issue.getType());

        assertNotNull(issue.getDescription());

        FlowIssueAdditionalProperties additionalProperties = (FlowIssueAdditionalProperties) issue.getAdditionalProperties();
        String flowPath = additionalProperties.getCallHierarchy(null);
        assertEquals(expectedFlowPath, flowPath);

        issue = iterator.next();
        additionalProperties = (FlowIssueAdditionalProperties) issue.getAdditionalProperties();
        flowPath = additionalProperties.getCallHierarchy(null);
        assertEquals(expectedFlowPath, flowPath);

        issue = iterator.next();
        additionalProperties = (FlowIssueAdditionalProperties) issue.getAdditionalProperties();
        flowPath = additionalProperties.getCallHierarchy(null);
        assertEquals(expectedFlowPath, flowPath);

    }

    /**
     * Verify that parser is serializable - it is needed for example for doing job on slave jenkins and transferring results to master.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @task 88834
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        serialization("jTest_10_static_2.xml");
    }

    @Test
    public void testSerializationJtest_10_5_2() throws IOException, ClassNotFoundException
    {
        serialization("jtest_10.5.2_static.xml");
    }

    public void serialization(String fileName) throws IOException, ClassNotFoundException
    {
        Properties settings = new Properties();
        settings.setProperty("rules.provider1a.analyzer", "com.puppycrawl.tools.checkstyle");
        settings.setProperty("rules.provider1a.separator", ".");
        settings.setProperty("rules.provider1a.data", "/home/jez/dv/dv-etest/com.parasoft.xtest.analyzers.checkstyle/rules/cs-rules.xml");
        ParasoftParser parserBefore = new ParasoftParser(settings, "workspace");
        Report reportBefore = parseFile(TEST_RESOURCES + "xml/" + fileName, parserBefore);

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(out);
            oos.writeObject(parserBefore);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ois = new ObjectInputStream(in);
            ParasoftParser parserAfter = (ParasoftParser) ois.readObject();
            Report reportAfter = parseFile(TEST_RESOURCES + "xml/" + fileName, parserAfter);

            assertEquals(reportBefore.size(), reportAfter.size());

            Properties propertiesBefore = parserBefore.getProperties();
            Properties propertiesAfter = parserAfter.getProperties();
            assertEquals(propertiesBefore.size(), propertiesAfter.size());
            Enumeration<Object> keys = propertiesBefore.keys();
            while (keys.hasMoreElements()) {
                String sKey = (String) keys.nextElement();
                assertTrue(propertiesAfter.containsKey(sKey));
                assertEquals(propertiesBefore.getProperty(sKey), propertiesAfter.getProperty(sKey));
            }
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
    }

    private static List<IViolation> mockViolations()
    {
        List<IViolation> violations = new ArrayList<IViolation>();
        IRuleViolation violationMock = mockViolation(// severity 5
            "INIT.AAI",
            "Array initializer used.",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java",
            PROJECT_NAME, 5, 10, "5", "Do not use array initializers.");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 5
            "INIT.AAI",
            "Array initializer used.",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftResult.java",
            PROJECT_NAME, 7, 12, "5", "Do not use array initializers.");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 3
            "INIT.AULI",
            "This &quot;if&quot; statement may mistakenly use &quot;!=&quot; instead of &quot;==&quot; for lazy initialization of &quot;{0}&quot;.",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java",
            PROJECT_NAME, 5, 10, "3", "Ensure that the &quot;if&quot; check for lazy initialization uses the correct operator.");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 3
            "SERIAL.SNSO",
            "Argument ''{0}'' to method &quot;setAttribute&quot; is non-serializable.",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftDescriptor.java",
            PROJECT_NAME, 5, 10, "3", "Do not store non-serializable objects as HttpSession attributes");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 1
            "SERIAL.OC",
            "Outer class ''{0}'' is not serializable.",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPublisher.java",
            PROJECT_NAME, 5, 10, "1", "Ensure outer class is serializable if its inner class is serializable");
        violations.add(violationMock);
        return violations;
    }

    private static IRuleViolation mockViolation(String ruleId, String message, String absolutePath, String packageName, int startingLine,
        int endingLine, String severity, String header)
    {
        IRuleViolation violationMock = Mockito.mock(IRuleViolation.class);

        Mockito.when(violationMock.getRuleId()).thenReturn(ruleId);
        Mockito.when(violationMock.getMessage()).thenReturn(message);
        IResultLocation resultLocationMock = mockResultLocation(startingLine, endingLine, absolutePath, packageName);
        Mockito.when(violationMock.getResultLocation()).thenReturn(resultLocationMock);

        String ruleCategory = substringBefore(ruleId, ".");

        Mockito.when(violationMock.getAttribute(IRuleAttributes.SEVERITY_ATTR)).thenReturn(severity);
        Mockito.when(violationMock.getAttribute(IRuleAttributes.RULE_CATEGORY_ATTR)).thenReturn(ruleCategory);
        Mockito.when(violationMock.getAttribute(IRuleAttributes.RULE_HEADER_ATTR)).thenReturn(header);

        return violationMock;
    }

    private static List<IViolation> mockFAViolations()
    {
        List<IViolation> violations = new ArrayList<IViolation>();

        List<String> childrenFileNames = new ArrayList<String>();
        childrenFileNames.add(PROJECT_PATH + RELATIVE_PATH + "\\ParasoftResult.java");
        childrenFileNames.add(PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPublisher.java");
        childrenFileNames.add(PROJECT_PATH + RELATIVE_PATH + "\\ParasoftHealthDescriptor.java");

        IFlowAnalysisViolation violationMock = mockFAViolation(// severity 5
            "FA.FA",
            "Null value carrier",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java",
            childrenFileNames, PROJECT_NAME, 10, 10);
        violations.add(violationMock);
        violationMock = mockFAViolation(// severity 5
            "FA.FA",
            "Null value carrier",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftResult.java",
            childrenFileNames, PROJECT_NAME, 12, 12);
        violations.add(violationMock);
        violationMock = mockFAViolation(// severity 3
            "FA.FA",
            "Null value carrier",
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java",
            childrenFileNames, PROJECT_NAME, 17, 17);
        violations.add(violationMock);
        return violations;
    }

    private static IFlowAnalysisViolation mockFAViolation(String ruleId, String message, String absolutePath, List<String> childrenFileNames,
        String packageName, int startingLine, int endingLine)
    {
        IResultLocation resultLocationMock = mockResultLocation(startingLine, endingLine, absolutePath, packageName);

        ruleId.split(".");
        String ruleCategory = substringBefore(ruleId, ".");

        Properties properties = new Properties();
        properties.setProperty(IRuleAttributes.SEVERITY_ATTR, "1");
        properties.setProperty(IRuleAttributes.RULE_CATEGORY_ATTR, ruleCategory);
        properties.setProperty(IRuleAttributes.RULE_HEADER_ATTR, "Flowanalysis stack trace:");

        IFlowAnalysisViolation violationMock = new FARuleViolationMock(resultLocationMock, packageName, properties, message, ruleId,
            getChildrenArray(childrenFileNames));

        return violationMock;
    }

    private static String substringBefore(String str, String separator)
    {
        int index = str.indexOf(separator);
        if (index == -1) {
            return str;
        }
        return str.substring(0, index);
    }

    private static IFlowAnalysisPathElement[] getChildrenArray(List<String> childrenFilePaths)
    {
        List<IFlowAnalysisPathElement> childrenMock = new ArrayList<IFlowAnalysisPathElement>();

        IFlowAnalysisPathElement[] emptyArray = new IFlowAnalysisPathElement[0];

        for (int i = 0; i < childrenFilePaths.size(); i++) {
            String childFilePath = childrenFilePaths.get(i);
            IResultLocation location = mockResultLocation(i + 5, i + 5, childFilePath, PROJECT_NAME);
            IFlowAnalysisPathElement descriptor = Mockito.mock(IFlowAnalysisPathElement.class);
            Mockito.when(descriptor.getLocation()).thenReturn(location);
            Mockito.when(descriptor.getDescription()).thenReturn("Element Description");
            Type type = Mockito.mock(Type.class);
            Mockito.when(type.getIdentifier()).thenReturn(".");
            Mockito.when(descriptor.getType()).thenReturn(type );

            if (i == 2) {
                List<String> childrenFileNames = new ArrayList<String>();
                childrenFileNames.add(PROJECT_PATH + RELATIVE_PATH + "\\ParasoftDetailBuilder.java");
                childrenFileNames.add(PROJECT_PATH + RELATIVE_PATH + "\\ParasoftProjectAction.java");
                IFlowAnalysisPathElement[] array = getChildrenArray(childrenFileNames);
                Mockito.when(descriptor.getChildren()).thenReturn(array);
            } else {
                Mockito.when(descriptor.getChildren()).thenReturn(emptyArray);
            }
            childrenMock.add(descriptor);
        }
        IFlowAnalysisPathElement[] array = childrenMock.toArray(new IFlowAnalysisPathElement[childrenMock.size()]);
        return array;
    }

    private static IResultLocation mockResultLocation(int startLine, int endLine, String absolutePath, String moduleName)
    {
        ISourceRange sourceRangeMock = Mockito.mock(ISourceRange.class);
        IProjectFileTestableInput fileTestableInputMock = Mockito.mock(IProjectFileTestableInput.class);
        File fileMock = Mockito.mock(File.class);
        Mockito.when(fileMock.getAbsolutePath()).thenReturn(absolutePath);
        Mockito.when(fileTestableInputMock.getFileLocation()).thenReturn(fileMock);
        Mockito.when(fileTestableInputMock.getProjectName()).thenReturn(moduleName);
        Path path = Paths.get(absolutePath);

        Mockito.when(fileTestableInputMock.getProjectRelativePath()).thenReturn(path.getFileName().toString());

        IResultLocation resultLocationMock = Mockito.mock(IResultLocation.class);
        Mockito.when(resultLocationMock.getSourceRange()).thenReturn(sourceRangeMock);
        Mockito.when(sourceRangeMock.getStartLine()).thenReturn(startLine);
        Mockito.when(sourceRangeMock.getEndLine()).thenReturn(endLine);
        Mockito.when(resultLocationMock.getTestableInput()).thenReturn(fileTestableInputMock);
        return resultLocationMock;
    }

}
