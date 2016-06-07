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
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.PluginDescriptor;

/**
 * Controls the life cycle of the Parasoft results. This action persists the results of the
 * Parasoft analysis of a build and displays the results on the build page. The actual
 * visualization of the results is defined in the matching <code>summary.jelly</code> file.
 * 
 * <p>
 * In addition this class renders the Parasoft result trend.
 * </p>
 */
public class ParasoftResultAction
    extends AbstractResultAction<ParasoftResult>
{
    /**
     * Creates a new instance of <code>ParasoftResultAction</code>.
     * 
     * @param owner the associated build of this action
     * @param healthDescriptor health descriptor to use
     * @param result the result in this build
     */
    public ParasoftResultAction(AbstractBuild<?, ?> owner, HealthDescriptor healthDescriptor, ParasoftResult result)
    {
        super(owner, new ParasoftHealthDescriptor(healthDescriptor), result);
    }

    /**
     * @see hudson.model.Action#getDisplayName()
     */
    public String getDisplayName()
    {
        return Messages.PARASOFT_PROJECT_ACTION_NAME;
    }

    @Override
    protected PluginDescriptor getDescriptor()
    {
        return new ParasoftDescriptor();
    }
}
