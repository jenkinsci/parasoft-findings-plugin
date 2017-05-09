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

package com.parasoft.xtest.reports.jenkins;

import java.util.Collection;

import hudson.model.Action;
import hudson.model.Run;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.HealthDescriptor;
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
    public ParasoftResultAction(Run<?, ?> owner, HealthDescriptor healthDescriptor, ParasoftResult result)
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
    
    @Override
    public Collection<? extends Action> getProjectActions()
    {
        return asSet(new ParasoftProjectAction(getJob()));
    }
}
