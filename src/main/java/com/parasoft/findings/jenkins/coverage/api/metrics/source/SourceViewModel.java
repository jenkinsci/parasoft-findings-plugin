/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.parasoft.findings.jenkins.coverage.api.metrics.source;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.parasoft.findings.jenkins.coverage.model.FileNode;

import hudson.model.ModelObject;
import hudson.model.Run;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import static com.parasoft.findings.jenkins.coverage.api.metrics.source.CoverageSourcePrinter.*;

/**
 * Server side model that provides the data for the source code view of the coverage results. The layout of the
 * associated view is defined corresponding jelly view 'index.jelly'.
 *
 * @author Ullrich Hafner
 */
public class SourceViewModel implements ModelObject {
    private static final SourceCodeFacade SOURCE_CODE_FACADE = new SourceCodeFacade();

    private final Run<?, ?> owner;
    private final String id;
    private final FileNode fileNode;
    private final CoverageScopeInSourceFile coverageScope;

    /**
     * Creates a new source view model instance.
     *
     * @param owner
     *         the owner of this view
     * @param id
     *         the ID that is used to store the coverage sources
     * @param fileNode
     *         the selected file node of the coverage tree
     */
    public SourceViewModel(final Run<?, ?> owner, final String id, final FileNode fileNode, CoverageScopeInSourceFile coverageScope) {
        this.owner = owner;
        this.id = id;
        this.fileNode = fileNode;
        this.coverageScope = coverageScope;
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    public FileNode getNode() {
        return fileNode;
    }

    /**
     * Returns the source file rendered in HTML.
     *
     * @return the colored source code as HTML document
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getSourceFileContent() {
        try {
            String sourceFileContent = SOURCE_CODE_FACADE.read(getOwner().getRootDir(), id, getNode().getRelativePath());
            if (CoverageScopeInSourceFile.MODIFIED_COVERAGE == coverageScope) {
                sourceFileContent = SOURCE_CODE_FACADE.calculateModifiedLinesCoverageSourceCode(sourceFileContent, getNode());
            }
            // Check if the environment is in English
            if (ALL_BRANCHES_COVERED.equals(Messages.All_Branches_Covered())) {
                return sourceFileContent;
            }
            // Localizing "tooltip" in source code.
            Document doc = Jsoup.parse(sourceFileContent, Parser.xmlParser());
            Elements trTags = doc.select("tr");
            for (Element tag : trTags) {
                String tooltipValue = tag.attr(TOOLTIP_ATTR);
                if (tooltipValue.isEmpty()){
                    continue;
                }
                if (tooltipValue.equals(ALL_BRANCHES_COVERED)) {
                    tag.attr(TOOLTIP_ATTR, Messages.All_Branches_Covered());
                } else if (tooltipValue.equals(PARTIALLY_COVERED_AND_BRANCH_COVERAGE)) {
                    tag.attr(TOOLTIP_ATTR, Messages.Partially_Covered_And_Branch_Coverage());
                } else if (tooltipValue.equals(COVERED_AT_LEAST_ONCE)) {
                    tag.attr(TOOLTIP_ATTR, Messages.Covered_At_Least_Once());
                } else if (tooltipValue.equals(NOT_COVERED)) {
                    tag.attr(TOOLTIP_ATTR, Messages.Not_Covered());
                }
            }
            return doc.html();
        }
        catch (IOException | InterruptedException exception) {
            return ExceptionUtils.getStackTrace(exception);
        }
    }

    /**
     * Returns whether the source file is available in Jenkins build folder.
     *
     * @return {@code true} if the source file is available, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isSourceFileAvailable() {
        return SOURCE_CODE_FACADE.canRead(getOwner().getRootDir(), id, fileNode.getRelativePath());
    }

    @Override
    public String getDisplayName() {
        return Messages.Coverage_Title(getNode().getName());
    }

    public enum CoverageScopeInSourceFile {
        MODIFIED_COVERAGE,
        OVERALL_COVERAGE
    }
}
