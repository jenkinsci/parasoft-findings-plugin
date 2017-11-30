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

package com.parasoft.xtest.reports.jenkins.parser;

import com.parasoft.xtest.common.api.IFileTestableInput;
import com.parasoft.xtest.common.api.IProjectFileTestableInput;
import com.parasoft.xtest.common.api.ISourceRange;
import com.parasoft.xtest.common.api.ITestableInput;
import com.parasoft.xtest.common.locations.ILocationAttributes;
import com.parasoft.xtest.common.math.ULong;
import com.parasoft.xtest.common.path.PathInput;
import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.internal.JenkinsLocationMatcher;
import com.parasoft.xtest.reports.jenkins.internal.JenkinsResultsImporter;
import com.parasoft.xtest.reports.jenkins.internal.ResultAdditionalAttributes;
import com.parasoft.xtest.reports.jenkins.internal.rules.JenkinsRulesUtil;
import com.parasoft.xtest.results.api.IDupCodeViolation;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IMetricsViolation;
import com.parasoft.xtest.results.api.IResultLocation;
import com.parasoft.xtest.results.api.IRuleViolation;
import com.parasoft.xtest.results.api.IViolation;
import com.parasoft.xtest.results.api.attributes.IRuleAttributes;
import com.parasoft.xtest.results.api.importer.IImportedData;
import com.parasoft.xtest.results.api.importer.IRulesImportHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import hudson.plugins.analysis.core.AbstractAnnotationParser;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for Parasoft files containing xml report.
 */
public class ParasoftParser
    extends AbstractAnnotationParser
{
    
    private static final String GLOBAL_CATEGORY = "GLOBAL"; //$NON-NLS-1$

    private static final String LEGACY_TOOL_NAME = "c++test"; //$NON-NLS-1$

    private final Properties _properties;

    private transient JenkinsResultsImporter _importer = null;

    /**
     * Creates a new instance of {@link ParasoftParser}.
     */
    public ParasoftParser()
    {
        this(StringUtils.EMPTY, new Properties());
    }

    /**
     * Creates a new instance of {@link ParasoftParser}.
     * 
     * @param sDefaultEncoding the default encoding to be used when reading and parsing files
     * @param properties settings to use while constructing annotations
     */
    public ParasoftParser(String sDefaultEncoding, Properties properties)
    {
        super(sDefaultEncoding);
        
        _properties = properties;
        Logger.getLogger().debug("Constructor call with settings: " + _properties); //$NON-NLS-1$
    }

    @Override
    public Collection<FileAnnotation> parse(File file, String sModuleName)
        throws InvocationTargetException
    {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            return intern(importResults(file, sModuleName));
        } catch (FileNotFoundException exception) {
            throw new InvocationTargetException(exception);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
    
    @Override
    public Collection<FileAnnotation> parse(InputStream file, String moduleName)
        throws InvocationTargetException
    {
        // unused
        return null;
    }

    /**
     * Converts the internal structure to the annotations API.
     * 
     * @param importResults list of violations
     * @param rulesImportHandler provides information about rules
     * @param sModuleName name of the maven module
     * @return a collection of results in format of the annotations API
     */
    public Collection<FileAnnotation> convert(Iterator<IViolation> importResults, IRulesImportHandler rulesImportHandler, String sModuleName)
    {
        JenkinsRulesUtil.refreshRuleDescriptions(_properties);
        
        List<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
        while (importResults.hasNext()) {
            IViolation result = importResults.next();
            IRuleViolation violation = null;
            if (result instanceof IRuleViolation){
                violation = (IRuleViolation)result;
            } else {
                Logger.getLogger().warn("Result is not instance of IRuleViolation"); //$NON-NLS-1$
                continue;
            }
            
            Warning warning = toWarning(violation, rulesImportHandler, sModuleName);
            if (warning == null) {
                continue;
            }
                
            annotations.add(warning);
        }
        return annotations;
    }
    
    /**
     * @return settings used by this instance of parser
     */
    public Properties getProperties()
    {
        return _properties;
    }

    private synchronized JenkinsResultsImporter getImporter()
    {
        if (_importer == null) {
            _importer = new JenkinsResultsImporter(_properties);
        }

        return _importer;
    }

    private static Warning toWarning(IRuleViolation violation, IRulesImportHandler rulesImportHandler, String sModuleName)
    {
        ResultAdditionalAttributes attributes = new ResultAdditionalAttributes(violation);
        if (attributes.isSuppressed()) {
            return null;
        }

        String message = violation.getMessage();
        int severity = attributes.getSeverity();
        Priority priority = convertSeverityToPriority(severity);
        String ruleCategory = attributes.getRuleCategory();
        
        IResultLocation location = violation.getResultLocation();
        ISourceRange sourceRange = location.getSourceRange();
        int startLine = sourceRange.getStartLine();
        int endLine = sourceRange.getEndLine();

        String ruleId = violation.getRuleId();
        String categoryDesc = rulesImportHandler.getCategoryDescription(ruleCategory);
        String ruleDesc = ruleId;
        
        Warning warning = new Warning(priority, message, startLine, endLine, categoryDesc, ruleDesc);
        
        String author = attributes.getAuthor();
        if (UString.isEmpty(author)) {
            author = PROPERTY_UNKNOWN;
        }
        warning.setAuthor(author);
        String revision = attributes.getRevision();
        if (UString.isEmpty(revision)) {
            revision = PROPERTY_UNKNOWN;
        }
        warning.setRevision(revision);
        
        String analyzer = violation.getAnalyzerId();
        if (isLegacyReport(analyzer)) {
            analyzer = mapToAnalyzer(violation, rulesImportHandler);
        }
        warning.setAnalyzer(analyzer);

        ITestableInput input = location.getTestableInput();
        if (input instanceof IFileTestableInput) {
            String sFilePath = JenkinsLocationMatcher.getFilePath((IFileTestableInput) input);
            if (sFilePath != null) {
                warning.setFileName(sFilePath);
            }
        } else if (input instanceof PathInput) {
            String workspacePath = ((PathInput)input).getPath();
            if (workspacePath.startsWith("/")) { //$NON-NLS-1$
                workspacePath = workspacePath.substring(1);
            }
            warning.setFileName(workspacePath);
        } else {
            warning.setFileName(input.getName());
        }

        if (input instanceof IProjectFileTestableInput) {
            warning.setModuleName(((IProjectFileTestableInput)input).getProjectName());
        } else {
            warning.setModuleName(sModuleName);
        }
        
        warning.setColumnPosition(sourceRange.getStartLineOffset(),
            sourceRange.getEndLineOffset());
        
        String namespace = violation.getNamespace();
        if (UString.isNonEmpty(namespace)){
            warning.setPackageName(namespace);
        } else {
            warning.setPackageName(PROPERTY_UNKNOWN);
        }
        warning.setToolTip(attributes.getRuleTitle());
        
        warning.populateViolationPathElements(violation);

        long hash = ULong.parseLong(violation.getAttribute(ILocationAttributes.LINE_HASH_ATTR), violation.hashCode());
        warning.setContextHashCode(hash);
        
        return warning;
    }

    private static String mapToAnalyzer(IRuleViolation violation, IRulesImportHandler rulesImportHandler) {
        String analyzer = null;
        if (violation instanceof IDupCodeViolation) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.dupcode"; //$NON-NLS-1$
        } else if (violation instanceof IFlowAnalysisViolation) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.flow"; //$NON-NLS-1$
        } else if (violation instanceof IMetricsViolation) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.metrics"; //$NON-NLS-1$
        } else if (isGlobalRule(violation.getRuleId(), rulesImportHandler)) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.global"; //$NON-NLS-1$
        } else {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.pattern"; //$NON-NLS-1$
        }
        return analyzer;
    }

    private static boolean isGlobalRule(String ruleId, IRulesImportHandler rulesImportHandler) {
        IRuleAttributes ruleAttributes = rulesImportHandler.getRuleAttributes(ruleId);
        if (ruleAttributes == null) {
            return false;
        }
        String ruleCategory = ruleAttributes.getRuleCategory();
        return GLOBAL_CATEGORY.equals(ruleCategory);
    }

    private static boolean isLegacyReport(String analyzer) {
        return LEGACY_TOOL_NAME.equals(analyzer);
    }

    private Collection<FileAnnotation> importResults(File file, String sModuleName)
    {
        IImportedData importedData = getImporter().performImport(file);
        if (importedData == null) {
            return Collections.emptyList();
        }
        if (UString.isEmpty(sModuleName)) {
            sModuleName = PROPERTY_UNKNOWN;
        }
        return convert(importedData, importedData.getRulesImportHandler(), sModuleName);
    }

    private static Priority convertSeverityToPriority(int severity)
    {
        switch (severity) {
            case 1:
            case 2:
                return Priority.HIGH;
            case 3:
                return Priority.NORMAL;
            default:
                return Priority.LOW;
        }
    }

    
    /** Unique ID of this class. */
    private static final long serialVersionUID = 6507147028628714704L;

    private static final String PROPERTY_UNKNOWN = "unknown"; //$NON-NLS-1$

}