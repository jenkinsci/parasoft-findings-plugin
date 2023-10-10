package com.parasoft.findings.jenkins.coverage.api.metrics.source;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.coverage.FileNode;

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
    public SourceViewModel(final Run<?, ?> owner, final String id, final FileNode fileNode) {
        this.owner = owner;
        this.id = id;
        this.fileNode = fileNode;
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
}
