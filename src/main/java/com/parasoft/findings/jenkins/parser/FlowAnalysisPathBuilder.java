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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.common.api.IFileTestableInput;
import com.parasoft.xtest.common.api.IProjectFileTestableInput;
import com.parasoft.xtest.common.api.ISourceRange;
import com.parasoft.xtest.common.api.ITestableInput;
import com.parasoft.xtest.common.collections.UCollection;
import com.parasoft.xtest.common.path.PathInput;
import com.parasoft.xtest.common.text.UString;
import com.parasoft.findings.jenkins.html.Colors;
import com.parasoft.findings.jenkins.html.IHtmlTags;
import com.parasoft.findings.jenkins.internal.JenkinsLocationMatcher;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement;
import com.parasoft.xtest.results.api.IFlowAnalysisPathElement.Type;
import com.parasoft.xtest.results.api.IFlowAnalysisViolation;
import com.parasoft.xtest.results.api.IPathElementAnnotation;
import com.parasoft.xtest.results.api.IResultLocation;

import edu.hm.hafner.analysis.FileNameResolver;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

public class FlowAnalysisPathBuilder
{
    private final IFlowAnalysisViolation _violation;
    private final String _parentKey;
    private Path _workspace;

    private static final String ANNOTATION_KIND_POINT = "point"; //$NON-NLS-1$
    private static final String ANNOTATION_KIND_CAUSE = "cause"; //$NON-NLS-1$
    private static final String ANNOTATION_KIND_EXCEPTION = "except"; //$NON-NLS-1$
    private static final String ANNOTATION_KIND_INFO = "info"; //$NON-NLS-1$
    private static final String ANNOTATION_SEPARATOR = " *** "; //$NON-NLS-1$

    private static List<String> excludedFromMessages = Arrays.asList(ANNOTATION_KIND_POINT, ANNOTATION_KIND_CAUSE);
    private static List<Character> importantPathElements = Arrays.asList(IFlowAnalysisPathElement.IMPORTANT_ELEMENT, IFlowAnalysisPathElement.POINT,
        IFlowAnalysisPathElement.THROWING_CHAR, IFlowAnalysisPathElement.CAUSE, IFlowAnalysisPathElement.RULE);

    /**
     * @param violation for which path information is needed
     * @param parentKey unique key of a warning that owns this violation
     */
    public FlowAnalysisPathBuilder(IFlowAnalysisViolation violation, String parentKey, Path workspace)
    {
        _violation = violation;
        _parentKey = parentKey;
        _workspace = workspace;
    }

    public List<Issue> getPath()
    {
        return getPath(_violation.getPathElements());
    }

    private String getAnnotationByKind(IFlowAnalysisPathElement descriptor, String kind)
    {
        for (IPathElementAnnotation annotation : descriptor.getAnnotations()) {
            if (annotation.getKind().equals(kind)) {
                return annotation.getMessage();
            }
        }
        return null;
    }

    private List<Issue> getChildren(IFlowAnalysisPathElement descriptor)
    {
        return getPath(descriptor.getChildren());
    }

    private String getDescription(IFlowAnalysisPathElement descriptor, boolean useAnnotation)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IHtmlTags.NON_BREAKABLE_SPACE);
        sb.append(IHtmlTags.CODE_START_TAG);

        if (!isImportant(descriptor)) {
            sb.append(Colors.createColorSpanStartTag(Colors.GRAY));
            sb.append(descriptor.getDescription());
            sb.append(IHtmlTags.SPAN_END_TAG);
        } else {
            sb.append(Colors.createColorSpanStartTag(Colors.BLACK));
            sb.append(descriptor.getDescription());
            sb.append(IHtmlTags.SPAN_END_TAG);
        }

        sb.append(IHtmlTags.CODE_END_TAG);
        String message = getMessage(descriptor, true, useAnnotation);

        if (message.length() > 0) {
            sb.append(message);
        }
        return sb.toString();
    }

    private String getExceptionMessageFromDescriptor(IFlowAnalysisPathElement descriptor)
    {
        String throwingMethod = descriptor.getThrowingMethod();
        String thrownTypes = descriptor.getThrownTypes();
        if (UString.isEmpty(throwingMethod) || UString.isEmpty(thrownTypes)) {
            return null;
        } else {
            return throwingMethod + "() throws: " + thrownTypes; //$NON-NLS-1$
        }
    }

    private String getMessage(IFlowAnalysisPathElement descriptor, boolean bFullDescription, boolean useAnnotation)
    {
        if (descriptor == null) {
            return IStringConstants.EMPTY;
        }

        if ((descriptor.getType() == null) || (descriptor.getType().getIdentifier() == null)) {
            return IStringConstants.EMPTY;
        }

        StringBuilder sb = new StringBuilder();

        if (useAnnotation) {
            addAnnotations(sb, descriptor, bFullDescription);
        } else {
            String identifier = descriptor.getType().getIdentifier();
            if (!bFullDescription) {
                if (identifier.contains(String.valueOf(IFlowAnalysisPathElement.POINT))) {
                    addStyledMessage(sb, _violation.getPointMessage(), bFullDescription, null, FontStyle.BLANK);
                }
                if (identifier.contains(String.valueOf(IFlowAnalysisPathElement.CAUSE))) {
                    addStyledMessage(sb, _violation.getCauseMessage(), bFullDescription, null, FontStyle.BLANK);
                }
            }

            if (identifier.contains(String.valueOf(IFlowAnalysisPathElement.RULE))) {
                addRuleImportantPointMessage(sb, _violation.getRuleImportantPointMessage(), bFullDescription);
            }

            if (identifier.contains(String.valueOf(IFlowAnalysisPathElement.THROWING_CHAR))) {
                String message = getExceptionMessageFromDescriptor(descriptor);
                addExceptionMessage(sb, message, bFullDescription);
            }

            addTrackedMessage(sb, descriptor, TRACKED_VARIABLES_ATTR, bFullDescription);
            addTrackedMessage(sb, descriptor, TRACKED_COLLECTIONS_ATTR, bFullDescription);
        }

        return sb.toString();
    }

    private List<Issue> getPath(IFlowAnalysisPathElement[] descriptors)
    {
        boolean useAnnotations = useAnnotations(descriptors);
        List<Issue> result = new ArrayList<Issue>();
        IssueBuilder issueBuilder = new IssueBuilder();
        for (IFlowAnalysisPathElement descriptor : descriptors) {
            Issue element = createElement(descriptor, useAnnotations, issueBuilder);
            result.add(element);
        }
        if (UCollection.isNonEmpty(result)) {
            createAbsolutePathsForIssues(result);
        }
        return result;
    }

    private void addAnnotations(StringBuilder sb, IFlowAnalysisPathElement descriptor, boolean bFullDescription)
    {
        if (descriptor.getAnnotations() == null) {
            return;
        }

        List<IPathElementAnnotation> annotations = new ArrayList<>(descriptor.getAnnotations());

        addImportantMessages(sb, annotations, bFullDescription, descriptor.getType().getIdentifier());
        addNormalMessages(sb, annotations, bFullDescription);
    }

    private void addExceptionMessage(StringBuilder sb, String message, boolean bFullDescription)
    {
        if (message != null) {
            addStyledMessage(sb, message, bFullDescription, Colors.GREEN, FontStyle.ITALIC_BOLD);
        }
    }

    private void addImportantMessages(StringBuilder sb, List<IPathElementAnnotation> annotations, boolean bFullDescription,
        String descriptorIdentifier)
    {
        for (Iterator<IPathElementAnnotation> it = annotations.iterator(); it.hasNext();) {
            IPathElementAnnotation annotation = it.next();
            String kind = annotation.getKind();
            if (kind == null) {
                it.remove();
            } else if (excludedFromMessages.contains(kind)) {
                if (!bFullDescription) {
                    addStyledMessage(sb, annotation.getMessage(), bFullDescription, null, FontStyle.BLANK);
                }
                it.remove();
            } else if (descriptorIdentifier.contains(String.valueOf(IFlowAnalysisPathElement.RULE)) && kind.equals(ANNOTATION_KIND_INFO)) {
                addRuleImportantPointMessage(sb, annotation.getMessage(), bFullDescription);
                it.remove();
            } else if (kind.equals(ANNOTATION_KIND_EXCEPTION)) {
                addExceptionMessage(sb, annotation.getMessage(), bFullDescription);
                it.remove();
            }
        }
    }

    private void addNormalMessage(StringBuilder sb, String message, boolean bFullDescription)
    {
        addStyledMessage(sb, message, bFullDescription, Colors.GREEN, FontStyle.ITALIC);
    }

    private void addNormalMessages(StringBuilder sb, List<IPathElementAnnotation> annotations, boolean bFullDescription)
    {
        for (IPathElementAnnotation annotation : annotations) {
            addNormalMessage(sb, annotation.getMessage(), bFullDescription);
        }
    }

    private void addRuleImportantPointMessage(StringBuilder sb, String message, boolean bFullDescription)
    {
        addStyledMessage(sb, message, bFullDescription, Colors.GREEN, FontStyle.BOLD);
    }

    private static void addStyledMessage(StringBuilder sb, String message, boolean bFullDescription, String color, FontStyle style)
    {
        if (UString.isEmpty(message)) {
            return;
        }

        if ((color != null) && bFullDescription) {
            sb.append(Colors.createColorSpanStartTag(color));
        }

        String sSeparator = IStringConstants.EMPTY;
        if (!bFullDescription && (sb.length() > 0)) {
            sSeparator = "; "; //$NON-NLS-1$
        } else if (bFullDescription) {
            sSeparator = ANNOTATION_SEPARATOR;
        }

        sb.append(sSeparator);

        if (bFullDescription) {
            if (style == FontStyle.BOLD) {
                sb.append(IHtmlTags.BOLD_START_TAG);
            } else if (style == FontStyle.ITALIC) {
                sb.append(IHtmlTags.ITALIC_START_TAG);
            } else if (style == FontStyle.ITALIC_BOLD) {
                sb.append(IHtmlTags.BOLD_START_TAG);
                sb.append(IHtmlTags.ITALIC_START_TAG);
            }
        }

        sb.append(message);

        if (bFullDescription) {
            if (style == FontStyle.BOLD) {
                sb.append(IHtmlTags.BOLD_END_TAG);
            } else if (style == FontStyle.ITALIC) {
                sb.append(IHtmlTags.ITALIC_END_TAG);
            } else if (style == FontStyle.ITALIC_BOLD) {
                sb.append(IHtmlTags.ITALIC_END_TAG);
                sb.append(IHtmlTags.BOLD_END_TAG);
            }
        }

        if ((color != null) && bFullDescription) {
            sb.append(IHtmlTags.SPAN_END_TAG);
        }
    }

    private void addTrackedMessage(StringBuilder sb, IFlowAnalysisPathElement descriptor, String attribute, boolean bFullDescription)
    {
        String message = _violation.getTrackedVariablesMessages().get(attribute);
        String variables = descriptor.getProperties().get(attribute);

        if (UString.isEmpty(variables)) {
            return;
        }
        String trackedMessage = message + IStringConstants.COLON_SP + variables;
        addNormalMessage(sb, trackedMessage, bFullDescription);
    }

    private void createAbsolutePathsForIssues(List<Issue> issues)
    {
        Report report = new Report();
        report.addAll(issues);
        FileNameResolver resolver = new FileNameResolver();
        resolver.run(report, _workspace.toString(), x -> false);
    }

    private Issue createElement(IFlowAnalysisPathElement descriptor, boolean useAnnotation, IssueBuilder issueBuilder)
    {
        IResultLocation location = descriptor.getLocation();
        FlowIssueAdditionalProperties additionalProperties = new FlowIssueAdditionalProperties();
        issueBuilder.setAdditionalProperties(additionalProperties);
        if (location != null) {
            String message = getMessage(descriptor, false, useAnnotation);
            issueBuilder.setMessage(message);
            ITestableInput input = location.getTestableInput();
            String filePath = null;
            if (input instanceof IFileTestableInput) {
                filePath = JenkinsLocationMatcher.getFilePath((IFileTestableInput) input);
            } else if (input instanceof PathInput) {
                filePath = ((PathInput) input).getPath();
                if (filePath.startsWith("/")) { //$NON-NLS-1$
                    filePath = filePath.substring(1);
                }
            } else {
                filePath = input.getName();
            }
            if (UString.isNonEmptyTrimmed(filePath)) {
                issueBuilder.setFileName(filePath);
            }

            if (input instanceof IProjectFileTestableInput) {
                IProjectFileTestableInput projectInput = (IProjectFileTestableInput) input;
                issueBuilder.setModuleName(projectInput.getProjectName());
            }

            ISourceRange sourceRange = location.getSourceRange();
            issueBuilder.setLineStart(sourceRange.getStartLine());
            issueBuilder.setLineEnd(sourceRange.getEndLine());
            issueBuilder.setColumnStart(sourceRange.getStartLineOffset());
            issueBuilder.setColumnEnd(sourceRange.getEndLineOffset());
            additionalProperties.setChildren(getChildren(descriptor));
            additionalProperties.setDescription(getDescription(descriptor, useAnnotation));

            String typeId = descriptor.getType().getIdentifier();
            if (typeId != null) {
                if (typeId.contains(String.valueOf(IFlowAnalysisPathElement.CAUSE))) {
                    if (useAnnotation) {
                        additionalProperties.setCause(getAnnotationByKind(descriptor, ANNOTATION_KIND_CAUSE));
                    } else {
                        additionalProperties.setCause(_violation.getCauseMessage());
                    }
                }

                if (typeId.contains(String.valueOf(IFlowAnalysisPathElement.POINT))) {
                    if (useAnnotation) {
                        additionalProperties.setPoint(getAnnotationByKind(descriptor, ANNOTATION_KIND_POINT));
                    } else {
                        additionalProperties.setPoint(_violation.getPointMessage());
                    }
                }
            }
        }
        additionalProperties.setParentKey(_parentKey);

        return issueBuilder.build();
    }

    private boolean useAnnotations(IFlowAnalysisPathElement[] descriptors)
    {
        for (IFlowAnalysisPathElement descriptor : descriptors) {
            if (CollectionUtils.isNotEmpty((descriptor.getAnnotations()))) {
                return true;
            }
        }

        return false;
    }

    private boolean isImportant(IFlowAnalysisPathElement descriptor)
    {
        Type type = descriptor.getType();

        if (type == null) {
            return false;
        }

        String typeId = descriptor.getType().getIdentifier();
        if (typeId == null) {
            return false;
        }

        for (Character importantChar : importantPathElements) {
            if (typeId.indexOf(importantChar) >= 0) {
                return true;
            }
        }

        return false;
    }

    enum FontStyle
    {
        BLANK, ITALIC, BOLD, ITALIC_BOLD
    }

    private static final String TRACKED_VARIABLES_ATTR = "Tracked variables"; //$NON-NLS-1$

    private static final String TRACKED_COLLECTIONS_ATTR = "Tracked collections"; //$NON-NLS-1$
}
