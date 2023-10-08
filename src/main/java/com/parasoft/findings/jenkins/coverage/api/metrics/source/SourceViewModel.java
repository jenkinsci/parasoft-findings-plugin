package com.parasoft.findings.jenkins.coverage.api.metrics.source;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.coverage.FileNode;

import hudson.model.ModelObject;
import hudson.model.Run;

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
            // Localizing "tooltip" in source code.
            sourceFileContent = sourceFileContent.replaceAll(PARASOFT_COVERAGE_ALL_BRANCHES_COVERED, Messages.All_Branches_Covered());
            sourceFileContent = sourceFileContent.replaceAll(PARASOFT_COVERAGE_PARTIALLY_COVERED_AND_BRANCH_COVERAGE, Messages.Partially_Covered_And_Branch_Coverage());
            sourceFileContent = sourceFileContent.replaceAll(PARASOFT_COVERAGE_COVERED_AT_LEAST_ONCE, Messages.Covered_At_Least_Once());
            sourceFileContent = sourceFileContent.replaceAll(PARASOFT_COVERAGE_NOT_COVERED, Messages.Not_Covered());
            return sourceFileContent;
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
