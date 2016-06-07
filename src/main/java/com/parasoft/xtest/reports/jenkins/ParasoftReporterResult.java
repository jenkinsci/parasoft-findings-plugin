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
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.BuildResult;

public class ParasoftReporterResult 
    extends ParasoftResult 
{

    /**
     * Creates a new instance of {@link ParasoftReporterResult}.
     *
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param useStableBuildAsReference determines whether only stable builds should be used as reference builds or not
     */
    public ParasoftReporterResult(AbstractBuild<?, ?> build, String defaultEncoding, ParserResult result, boolean useStableBuildAsReference) 
    {
        super(build, defaultEncoding, result, useStableBuildAsReference, ParasoftMavenResultAction.class);
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() 
    {
        return ParasoftMavenResultAction.class;
    }
    
    private static final long serialVersionUID = -6141604013001874832L;
}

