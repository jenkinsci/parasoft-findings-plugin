/*
 * Copyright 2016 Parasoft Corporation
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

import com.parasoft.xtest.reports.jenkins.parser.Warning;
import com.thoughtworks.xstream.XStream;

import hudson.model.Run;
import hudson.plugins.analysis.core.*;

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
     * @param usePreviousBuildAsReference determines whether only previous builds should be used as
     *        reference builds or not
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *        reference builds or not
     */
    public ParasoftResult(Run<?, ?> build, String defaultEncoding, ParserResult result,
        boolean usePreviousBuildAsReference, boolean useStableBuildAsReference)
    {
        this(build, defaultEncoding, result, usePreviousBuildAsReference,
            useStableBuildAsReference, ParasoftResultAction.class);
    }

    /**
     * Creates a new instance of {@link ParasoftResult}.
     * 
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param usePreviousBuildAsReference determines whether previous builds should be used as
     *        reference builds or not
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *        reference builds or not
     * @param actionType the type of the result action
     */
    protected ParasoftResult(final Run<?, ?> build, final String defaultEncoding,
        final ParserResult result, final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
        final Class<? extends ResultAction<ParasoftResult>> actionType)
    {
        this(build, new BuildHistory(
            build, actionType, usePreviousBuildAsReference, useStableBuildAsReference), result,
            defaultEncoding, true);
    }

    private ParasoftResult(final Run<?, ?> build, final BuildHistory history,
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
