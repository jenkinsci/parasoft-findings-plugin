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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hamcrest.collection.IsIn;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.parasoft.xtest.common.api.IProjectFileTestableInput;
import com.parasoft.xtest.common.api.ISourceRange;
import com.parasoft.xtest.common.iterators.IteratorUtil;
import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;
import com.parasoft.xtest.reports.jenkins.parser.Warning;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement.Type;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IResultLocation;
import com.parasoft.xtest.results.api.IRuleViolation;
import com.parasoft.xtest.results.api.IViolation;
import com.parasoft.xtest.results.api.attributes.IRuleAttributes;
import com.parasoft.xtest.results.api.importer.IRulesImportHandler;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the extraction of Parasoft analysis results.
 */
@SuppressWarnings("nls")
public class ParasoftParserTest
{

    private static final String TEST_RESOURCES = "src/test/resources/";

    private static final String PROJECT_NAME = "com.parasoft.xtest.reports.jenkins"; //$NON-NLS-1$

    private static final String PROJECT_PATH = "E:\\Eclipse Workspace\\com.parasoft.xtest.reports.jenkins"; //$NON-NLS-1$

    private static final String RELATIVE_PATH = "\\src\\main\\java\\com\\parasoft\\xtest\\reports\\jenkins"; //$NON-NLS-1$

    private static ParasoftParser _parser = null;

    @BeforeClass
    public static void initialize()
    {
        _parser = new ParasoftParser();
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
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/jTest_10_static_2.xml"); //$NON-NLS-1$
        assertEquals(503, IteratorUtil.countElements(annotations.iterator()));
    }

    @Test
    public void parseFAViolReportTest()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/jTest_10_static.xml"); //$NON-NLS-1$
        assertEquals(65, IteratorUtil.countElements(annotations.iterator()));
    }
    
    @Test
    public void parseCppEngineTestStatic()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.4_engine_static.xml"); //$NON-NLS-1$
        assertEquals(5, IteratorUtil.countElements(annotations.iterator()));
    }

    /**
     * @task 23266
     */
    @Test
    public void parseFAViolReportTest2()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/jTest_10_static_empty_element.xml"); //$NON-NLS-1$
        assertEquals(16, IteratorUtil.countElements(annotations.iterator()));
        for (FileAnnotation fileAnnotation : annotations) {
            assertTrue(fileAnnotation instanceof Warning);
            Warning warning = (Warning)fileAnnotation;
            FileAnnotation previousCall = warning.getPreviousCall(warning.getKey() + 1);
            assertNotNull(previousCall);
            assertTrue(UString.isEmpty(previousCall.getToolTip()));
        }
    }
    
    @Test
    public void parseCppDesktopStdViolsTest()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_static.xml"); //$NON-NLS-1$
        
        assertEquals(7, IteratorUtil.countElements(annotations.iterator()));
        for (FileAnnotation fileAnnotation : annotations) {
            assertTrue(fileAnnotation instanceof Warning);
            Warning warning = (Warning)fileAnnotation;
            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", warning.getAnalyzer()); //$NON-NLS-1$
        }
    }

    @Test
    public void parseCppDesktopStdViolsCategoriesTest_Old()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_static.xml"); //$NON-NLS-1$

        String[] rules = {"CODSTA-39", "CODSTA-39", "CODSTA-39", "MISRA2004-15_2", "INIT-04", "INIT-04", "INIT-04"}; 
        assertEquals(7, IteratorUtil.countElements(annotations.iterator()));
        int i = 0;
        for (FileAnnotation fileAnnotation : annotations) {
            assertTrue(fileAnnotation instanceof Warning);
            Warning warning = (Warning)fileAnnotation;
            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", warning.getAnalyzer()); //$NON-NLS-1$
            
            assertEquals(rules[i], warning.getType());
            assertEquals("-", warning.getCategory());
            i++;
        }
    }

    @Test
    public void parseCppDesktopStdViolsCategoriesTest()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES + "xml/cppTest_10.3.2_desktop_static_categories.xml"); //$NON-NLS-1$
        
        assertEquals(7, IteratorUtil.countElements(annotations.iterator()));
        for (FileAnnotation fileAnnotation : annotations) {
            assertTrue(fileAnnotation instanceof Warning);
            Warning warning = (Warning)fileAnnotation;
            assertEquals("com.parasoft.xtest.cpp.analyzer.static.pattern", warning.getAnalyzer()); //$NON-NLS-1$
            assertThat(warning.getCategory(), IsIn.isIn(new String[] {"Coding Conventions","Initialization", "MISRA C 2004"})); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
        }
    }
    
    
    @Test
    public void parseCppDesktopFAViolsTest()
    {
        Collection<FileAnnotation> annotations = parseFile(TEST_RESOURCES +  "xml/cppTest_10.3.2_desktop_flowanalysis.xml"); //$NON-NLS-1$
        
        assertEquals(4, IteratorUtil.countElements(annotations.iterator()));
        for (FileAnnotation fileAnnotation : annotations) {
            assertTrue(fileAnnotation instanceof Warning);
            Warning warning = (Warning)fileAnnotation;
            assertEquals("com.parasoft.xtest.cpp.analyzer.static.flow", warning.getAnalyzer()); //$NON-NLS-1$
        }
    }

    private Collection<FileAnnotation> parseFile(String name)
    {
        return parseFile(name, _parser);
    }

    private static Collection<FileAnnotation> parseFile(String name, ParasoftParser parser)
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
            return parser.parse(file, PROJECT_NAME);
        } catch (URISyntaxException | InvocationTargetException e) {
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
        Collection<FileAnnotation> annotations = _parser.convert(violations.iterator(), rulesImportHandlerMock, "");
        Iterator<FileAnnotation> iterator = annotations.iterator();

        FileAnnotation fileAnnotation = iterator.next();
        assertEquals(PROJECT_NAME, fileAnnotation.getModuleName());
        assertEquals(Priority.LOW, fileAnnotation.getPriority());
        assertNotNull(fileAnnotation.getPackageName());

        assertEquals("Initialize", fileAnnotation.getCategory());
        assertEquals("ParasoftPlugin.java", fileAnnotation.getShortFileName());
        assertEquals("INIT.AAI", fileAnnotation.getType());
        assertNotNull(fileAnnotation.getToolTip());

        fileAnnotation = iterator.next();
        assertEquals(Priority.LOW, fileAnnotation.getPriority());

        fileAnnotation = iterator.next();
        assertEquals(Priority.NORMAL, fileAnnotation.getPriority());

        fileAnnotation = iterator.next();
        assertEquals(Priority.NORMAL, fileAnnotation.getPriority());

        fileAnnotation = iterator.next();
        assertEquals(Priority.HIGH, fileAnnotation.getPriority());
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
        Collection<FileAnnotation> annotations = _parser.convert(violations.iterator(), rulesImportHandlerMock, "");
        Iterator<FileAnnotation> iterator = annotations.iterator();

        Warning warning = (Warning)iterator.next();
        assertEquals(PROJECT_NAME, warning.getModuleName());
        assertEquals(Priority.HIGH, warning.getPriority());
        assertNotNull(warning.getPackageName());
        assertEquals("Flow Analysis", warning.getCategory());
        assertEquals("ParasoftPlugin.java", warning.getShortFileName());
        assertEquals("FA.FA", warning.getType());

        assertNotNull(warning.getToolTip());
        for (int i = 0; i < 3; i++) {
            String toolTip = warning.getToolTip();
            for (int j = 0; j < 50; j++) {
                CharSequence sequence = "link." + warning.getKey() + "." + j + "/";
                if (toolTip.contains(sequence)) {
                    assertNotNull(warning.getPreviousCall(j));
                }
            }
            if (iterator.hasNext()) {
                warning = (Warning)iterator.next();
            }
        }
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
        Properties settings = new Properties();
        settings.setProperty("rules.provider1a.analyzer", "com.puppycrawl.tools.checkstyle");
        settings.setProperty("rules.provider1a.separator", ".");
        settings.setProperty("rules.provider1a.data", "/home/jez/dv/dv-etest/com.parasoft.xtest.analyzers.checkstyle/rules/cs-rules.xml");
        ParasoftParser parserBefore = new ParasoftParser("PL-123", settings);
        Collection<FileAnnotation> parsedBefore = parseFile(TEST_RESOURCES + "xml/jTest_10_static_2.xml", parserBefore);

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(out);
			oos.writeObject(parserBefore);

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ois = new ObjectInputStream(in);
			ParasoftParser parserAfter = (ParasoftParser) ois.readObject();
			Collection<FileAnnotation> parsedAfter = parseFile(TEST_RESOURCES + "xml/jTest_10_static_2.xml",
					parserAfter);

			assertEquals(parsedBefore.size(), parsedAfter.size());

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
            "INIT.AAI", //$NON-NLS-1$
            "Array initializer used.", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java", //$NON-NLS-1$
            PROJECT_NAME, 5, 10, "5", "Do not use array initializers.");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 5
            "INIT.AAI", //$NON-NLS-1$
            "Array initializer used.", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftResult.java", //$NON-NLS-1$
            PROJECT_NAME, 7, 12, "5", "Do not use array initializers.");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 3
            "INIT.AULI", //$NON-NLS-1$
            "This &quot;if&quot; statement may mistakenly use &quot;!=&quot; instead of &quot;==&quot; for lazy initialization of &quot;{0}&quot;.", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java", //$NON-NLS-1$
            PROJECT_NAME, 5, 10, "3", "Ensure that the &quot;if&quot; check for lazy initialization uses the correct operator.");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 3
            "SERIAL.SNSO", //$NON-NLS-1$
            "Argument ''{0}'' to method &quot;setAttribute&quot; is non-serializable.", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftDescriptor.java", //$NON-NLS-1$
            PROJECT_NAME, 5, 10, "3", "Do not store non-serializable objects as HttpSession attributes");
        violations.add(violationMock);
        violationMock = mockViolation(// severity 1
            "SERIAL.OC", //$NON-NLS-1$
            "Outer class ''{0}'' is not serializable.", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPublisher.java", //$NON-NLS-1$
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
            "FA.FA", //$NON-NLS-1$
            "Null value carrier", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java", //$NON-NLS-1$
            childrenFileNames, PROJECT_NAME, 10, 10);
        violations.add(violationMock);
        violationMock = mockFAViolation(// severity 5
            "FA.FA", //$NON-NLS-1$
            "Null value carrier", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftResult.java", //$NON-NLS-1$
            childrenFileNames, PROJECT_NAME, 12, 12);
        violations.add(violationMock);
        violationMock = mockFAViolation(// severity 3
            "FA.FA", //$NON-NLS-1$
            "Null value carrier", //$NON-NLS-1$
            PROJECT_PATH + RELATIVE_PATH + "\\ParasoftPlugin.java", //$NON-NLS-1$
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
