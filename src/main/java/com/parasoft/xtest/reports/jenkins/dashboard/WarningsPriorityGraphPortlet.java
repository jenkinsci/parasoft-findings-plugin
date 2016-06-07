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
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.PriorityGraph;
import hudson.plugins.view.dashboard.DashboardPortlet;

import org.kohsuke.stapler.DataBoundConstructor;

import com.parasoft.xtest.reports.jenkins.ParasoftDescriptor;
import com.parasoft.xtest.reports.jenkins.ParasoftProjectAction;

/**
 * Our implementation of portlet that shows the warnings trend graph by priority.
 */
public final class WarningsPriorityGraphPortlet 
    extends AbstractWarningsGraphPortlet 
{

    /**
     * Creates a new instance of {@link WarningsPriorityGraphPortlet}.
     *
     * @param name the name of the portlet
     * @param width width of the graph
     * @param height height of the graph
     * @param dayCountString number of days to consider
     */
    @DataBoundConstructor
    public WarningsPriorityGraphPortlet(String name, String width, String height, String dayCountString) 
    {
        super(name, width, height, dayCountString);

        configureGraph(getGraphType());
    }

    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() 
    {
        return ParasoftProjectAction.class;
    }

    @Override
    protected String getPluginName() 
    {
        return ParasoftDescriptor.PLUGIN_ID;
    }

    @Override
    protected BuildResultGraph getGraphType() 
    {
        return new PriorityGraph();
    }

    @Extension(optional = true)
    public static class WarningsGraphDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() 
        {
            return Messages.PORTLET_WARNINGS_PRIORITY_GRAPH;
        }
    }
}

