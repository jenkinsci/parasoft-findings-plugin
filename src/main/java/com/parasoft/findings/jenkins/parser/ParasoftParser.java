/*
 * Copyright 2019 Parasoft Corporation
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

package com.parasoft.findings.jenkins.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Properties;

import com.parasoft.findings.utils.results.violations.*;
import com.parasoft.findings.utils.results.testableinput.IFileTestableInput;
import com.parasoft.findings.utils.results.testableinput.ProjectFileTestableInput;
import com.parasoft.findings.utils.results.testableinput.ITestableInput;
import com.parasoft.findings.utils.results.testableinput.PathInput;
import com.parasoft.findings.utils.common.util.StringUtil;
import com.parasoft.findings.utils.results.testableinput.FindingsLocationMatcher;
import com.parasoft.findings.jenkins.internal.ResultAdditionalAttributes;
import com.parasoft.findings.utils.results.xml.RuleAttributes;
import com.parasoft.findings.utils.results.xml.RulesImportHandler;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

/**
 * A parser for Parasoft files containing xml report.
 */
public class ParasoftParser // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
        extends IssueParser
{
    private static final long serialVersionUID = 1731087921659486425L;

    private static final String GLOBAL_CATEGORY = "GLOBAL"; //$NON-NLS-1$

    private static final String LEGACY_TOOL_NAME = "c++test"; //$NON-NLS-1$

    private final Properties _properties;

    private final String _workspace;

    private transient XmlReportViolationsImporter _importer = null;

    public ParasoftParser(Properties properties, String workspace)
    {
        _properties = properties == null ? new Properties() : properties;
        Logger.getLogger().debug("Constructor call with settings: " + _properties); //$NON-NLS-1$
        _workspace = workspace;
    }

    public Properties getProperties()
    {
        return _properties;
    }

    @Override
    public Report parse(ReaderFactory readerFactory) throws ParsingException, ParsingCanceledException
    {
        File file = new File(readerFactory.getFileName());
        try (FileInputStream input = new FileInputStream(file)) {
            return importResults(file);
        } catch (IOException exception) {
            throw new ParsingException(exception);
        }
    }

    private Report importResults(File file)
    {
        XmlReportViolations importedData = getImporter().performImport(file);
        if (importedData == null) {
            return new Report();
        }
        return convert(importedData, importedData.getRulesImportHandler());
    }

    // keep it public for JUnit tests
    public Report convert(Iterator<IViolation> importResults, RulesImportHandler rulesImportHandler)
    {
        IssueBuilder issueBuilder = new IssueBuilder();
        Report report = new Report();

        while (importResults.hasNext()) {
            IViolation result = importResults.next();
            IRuleViolation violation = null;
            if (result instanceof IRuleViolation) {
                violation = (IRuleViolation) result;
            } else {
                Logger.getLogger().warn("Result is not instance of IRuleViolation"); //$NON-NLS-1$
                continue;
            }
            if (reportViolation(violation, rulesImportHandler, "-", issueBuilder)) { //$NON-NLS-1$
                Issue issue = issueBuilder.build();
                populateViolationPathElements(violation, issue);
                report.add(issue);
            }
        }
        return report;
    }

    private void populateViolationPathElements(IRuleViolation violation, Issue issue)
    {
        Serializable properties = issue.getAdditionalProperties();
        Path workspacePath = _workspace == null ? null : new File(_workspace).toPath();
        if (properties instanceof FlowIssueAdditionalProperties) {
            FlowIssueAdditionalProperties additionalProperties = (FlowIssueAdditionalProperties) properties;
            additionalProperties
                    .setChildren(new FlowAnalysisPathBuilder((IFlowAnalysisViolation) violation, issue.getId().toString(), workspacePath).getPath());
        } else if (properties instanceof DupIssueAdditionalProperties) {
            DupIssueAdditionalProperties additionalProperties = (DupIssueAdditionalProperties)properties;
            additionalProperties.setChildren(new DupCodePathBuilder((DupCodeViolation)violation, issue.getId().toString()).getPath());
        }
    }

    private boolean reportViolation(IRuleViolation violation, RulesImportHandler rulesImportHandler, String moduleName, IssueBuilder issueBuilder)
    {
        ResultAdditionalAttributes attributes = new ResultAdditionalAttributes(violation);
        if (attributes.isSuppressed()) {
            return false;
        }

        String message = violation.getMessage();
        int severity = attributes.getSeverity();
        Severity severityLevel = convertToSeverityLevel(severity);
        String ruleCategory = attributes.getRuleCategory();

        ResultLocation location = violation.getResultLocation();
        SourceRange sourceRange = location.getSourceRange();
        int startLine = sourceRange.getStartLine();
        int endLine = sourceRange.getEndLine();

        String ruleId = violation.getRuleId();
        String categoryDesc = rulesImportHandler.getCategoryDescription(ruleCategory);
        String ruleDesc = ruleId;

        issueBuilder.setSeverity(severityLevel).setMessage(message).setLineStart(startLine).setLineEnd(endLine).setCategory(categoryDesc)
                .setType(ruleDesc);

        ITestableInput input = location.getTestableInput();
        String filePath = null;
        if (input instanceof IFileTestableInput) {
            filePath = FindingsLocationMatcher.getFilePath((IFileTestableInput) input);
        } else if (input instanceof PathInput) {
            filePath = ((PathInput) input).getPath();
            if (filePath.startsWith("/")) { //$NON-NLS-1$
                filePath = filePath.substring(1);
            }
        } else {
            filePath = input.getName();
        }
        if (StringUtil.isNonEmptyTrimmed(filePath)) {
            issueBuilder.setFileName(filePath);
        }

        if (input instanceof ProjectFileTestableInput) {
            ProjectFileTestableInput projectInput = (ProjectFileTestableInput) input;
            issueBuilder.setModuleName(projectInput.getProjectName());
        } else {
            issueBuilder.setModuleName(moduleName);
        }

        issueBuilder.setColumnStart(sourceRange.getStartLineOffset());
        issueBuilder.setColumnEnd(sourceRange.getEndLineOffset());

        String namespace = violation.getNamespace();
        if (StringUtil.isNonEmpty(namespace)) {
            issueBuilder.setPackageName(namespace);
        } else {
            issueBuilder.setPackageName("-"); //$NON-NLS-1$
        }

        String author = attributes.getAuthor();
        if (StringUtil.isEmpty(author)) {
            author = PROPERTY_UNKNOWN;
        }

        String revision = attributes.getRevision();
        if (StringUtil.isEmpty(revision)) {
            revision = PROPERTY_UNKNOWN;
        }

        String analyzer = violation.getAnalyzerId();
        if (isLegacyReport(analyzer)) {
            analyzer = mapToAnalyzer(violation, rulesImportHandler);
        }
        issueBuilder.setDescription(attributes.getRuleTitle());

        if (violation instanceof IFlowAnalysisViolation) {
            issueBuilder.setAdditionalProperties(new FlowIssueAdditionalProperties(author, revision, analyzer));
        } else if (violation instanceof DupCodeViolation) {
            issueBuilder.setAdditionalProperties(new DupIssueAdditionalProperties(author, revision, analyzer));
        } else {
            issueBuilder.setAdditionalProperties(new ParasoftIssueAdditionalProperties(author, revision, analyzer));
        }

        return true;
    }

    private Severity convertToSeverityLevel(int severity)
    {
        switch (severity) {
            case 1:
            case 2:
                return Severity.WARNING_HIGH;
            case 3:
                return Severity.WARNING_NORMAL;
            default:
                return Severity.WARNING_LOW;
        }
    }

    private synchronized XmlReportViolationsImporter getImporter()
    {
        if (_importer == null) {
            _importer = new XmlReportViolationsImporter(_properties);
        }

        return _importer;
    }

    private static boolean isLegacyReport(String analyzer)
    {
        return LEGACY_TOOL_NAME.equals(analyzer);
    }

    private String mapToAnalyzer(IRuleViolation violation, RulesImportHandler rulesImportHandler)
    {
        String analyzer = null;
        if (violation instanceof DupCodeViolation) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.dupcode"; //$NON-NLS-1$
        } else if (violation instanceof IFlowAnalysisViolation) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.flow"; //$NON-NLS-1$
        } else if (violation instanceof MetricsViolation) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.metrics"; //$NON-NLS-1$
        } else if (isGlobalRule(violation.getRuleId(), rulesImportHandler)) {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.global"; //$NON-NLS-1$
        } else {
            analyzer = "com.parasoft.xtest.cpp.analyzer.static.pattern"; //$NON-NLS-1$
        }
        return analyzer;
    }

    private boolean isGlobalRule(String ruleId, RulesImportHandler rulesImportHandler)
    {
        RuleAttributes ruleAttributes = rulesImportHandler.getRuleAttributes(ruleId);
        if (ruleAttributes == null) {
            return false;
        }
        String ruleCategory = ruleAttributes.getRuleCategory();
        return GLOBAL_CATEGORY.equals(ruleCategory);
    }

    private static final String PROPERTY_UNKNOWN = "unknown"; //$NON-NLS-1$
}