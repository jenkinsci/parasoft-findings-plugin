/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.BuildResult;

import com.parasoft.xtest.reports.jenkins.parser.Warning;
import com.thoughtworks.xstream.XStream;

/**
 * Represents the results of the Parasoft analysis. 
 * One instance of this class is persisted for each build via an XML file.
 */
public class ParasoftResult
    extends BuildResult
{

    /**
     * Creates a new instance of {@link ParasoftResult}.
     * 
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *        reference builds or not
     */
    public ParasoftResult(AbstractBuild<?, ?> build, String defaultEncoding, ParserResult result, boolean useStableBuildAsReference)
    {
        this(build, defaultEncoding, result, useStableBuildAsReference, ParasoftResultAction.class);
    }

    /**
     * Creates a new instance of {@link ParasoftResult}.
     * 
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *        reference builds or not
     * @param actionType the type of the result action
     */
    protected ParasoftResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
        final ParserResult result, final boolean useStableBuildAsReference,
        final Class<? extends ResultAction<ParasoftResult>> actionType)
    {
        this(build, new BuildHistory(build, actionType, useStableBuildAsReference), result,
            defaultEncoding, true);
    }

    private ParasoftResult(final AbstractBuild<?, ?> build, final BuildHistory history,
        final ParserResult result, final String defaultEncoding, final boolean canSerialize)
    {
        super(build, history, result, defaultEncoding);

        if (canSerialize) {
            serializeAnnotations(result.getAnnotations());
        }
    }

    @Override
    public String getHeader()
    {
        return Messages.PARASOFT_RESULT_ACTION_HEADER;
    }

    @Override
    protected void configure(final XStream xstream)
    {
        xstream.alias("warning", Warning.class);  //$NON-NLS-1$
    }

    @Override
    public String getSummary()
    {
        String sSummaryBase = createDefaultSummary(ParasoftDescriptor.RESULT_URL, getNumberOfAnnotations(), getNumberOfModules());
        return "Parasoft: " + sSummaryBase; //$NON-NLS-1$
    }

    @Override
    protected String createDeltaMessage()
    {
        return createDefaultDeltaMessage(ParasoftDescriptor.RESULT_URL, getNumberOfNewWarnings(), getNumberOfFixedWarnings());
    }

    @Override
    protected String getSerializationFileName()
    {
        return PARASOFT_WARNINGS_XML;  
    }

    /**
     * @see hudson.model.ModelObject#getDisplayName()
     */
    public String getDisplayName()
    {
        return Messages.PARASOFT_PROJECT_ACTION_NAME;
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType()
    {
        return ParasoftResultAction.class;
    }
    
    private static final long serialVersionUID = 2768250056765266658L;
    private static final String PARASOFT_WARNINGS_XML = "parasoft-warnings.xml"; //$NON-NLS-1$
}
