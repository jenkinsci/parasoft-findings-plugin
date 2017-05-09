/*
 * Copyright 2017 Parasoft Corporation
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

package com.parasoft.xtest.reports.jenkins.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.NewVersusFixedGraph;
import hudson.plugins.view.dashboard.DashboardPortlet;

import org.kohsuke.stapler.DataBoundConstructor;

import com.parasoft.xtest.reports.jenkins.ParasoftDescriptor;
import com.parasoft.xtest.reports.jenkins.ParasoftProjectAction;

/**
 * Our implementation of portlet that shows the warnings trend graph of fixed versus new warnings.
 */
public final class WarningsNewVersusFixedGraphPortlet 
    extends AbstractWarningsGraphPortlet 
{

    /**
     * Creates a new instance of {@link WarningsNewVersusFixedGraphPortlet}.
     *
     * @param name the name of the portlet
     * @param width width of the graph
     * @param height height of the graph
     * @param dayCountString number of days to consider
     */
    @DataBoundConstructor
    public WarningsNewVersusFixedGraphPortlet(String name, String width, String height, String dayCountString) 
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
        return new NewVersusFixedGraph();
    }

    @Extension(optional = true)
    public static class WarningsGraphDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() 
        {
            return Messages.PORTLET_WARNINGS_NEW_VS_FIXED_GRAPH;
        }
    }
}

