/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.Launcher;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.AnnotationsAggregator;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;

/**
 * Aggregates {@link ParasoftResultAction}s of {@link MatrixRun}s into {@link MatrixBuild}.
 */
public class ParasoftAnnotationsAggregator
    extends AnnotationsAggregator
{
    /**
     * Creates a new instance of {@link ParasoftAnnotationsAggregator}.
     * 
     * @param build the matrix build
     * @param launcher the launcher
     * @param listener the build listener
     * @param healthDescriptor health descriptor
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *        reference builds or not
     */
    public ParasoftAnnotationsAggregator(final MatrixBuild build, final Launcher launcher,
        final BuildListener listener, final HealthDescriptor healthDescriptor,
        final String defaultEncoding, final boolean useStableBuildAsReference)
    {
        super(build, launcher, listener, healthDescriptor, defaultEncoding,
            useStableBuildAsReference);
    }

    @Override
    protected Action createAction(final HealthDescriptor healthDescriptor,
        final String defaultEncoding, final ParserResult aggregatedResult)
    {
        boolean bOnlyStable = useOnlyStableBuildsAsReference();
        ParasoftResult result = new ParasoftResult(build, defaultEncoding, aggregatedResult, bOnlyStable);
        return new ParasoftResultAction(build, healthDescriptor, result);
    }

    @Override
    protected boolean hasResult(final MatrixRun run)
    {
        return getAction(run) != null;
    }

    @Override
    protected ParasoftResult getResult(final MatrixRun run)
    {
        return getAction(run).getResult();
    }

    private static ParasoftResultAction getAction(final MatrixRun run)
    {
        return run.getAction(ParasoftResultAction.class);
    }
}
