/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsTablePortlet;
import hudson.plugins.view.dashboard.DashboardPortlet;

import org.kohsuke.stapler.DataBoundConstructor;

import com.parasoft.xtest.reports.jenkins.ParasoftProjectAction;

/**
 * Our implementation of portlet that shows a table with the number of warnings in a job.
 */
public class WarningsTablePortlet 
    extends AbstractWarningsTablePortlet 
{
    /**
     * Creates a new instance of {@link WarningsTablePortlet}.
     *
     * @param name the name of the portlet
     * @param canHideZeroWarningsProjects determines if zero warnings projects should be hidden in the table
     */
    @DataBoundConstructor
    public WarningsTablePortlet(String name, boolean canHideZeroWarningsProjects) 
    {
        super(name, canHideZeroWarningsProjects);
    }

    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() 
    {
        return ParasoftProjectAction.class;
    }

    @Extension(optional = true)
    public static class WarningsPerJobDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() 
        {
            return Messages.PORTLET_WARNINGS_TABLE;
        }
    }
}

