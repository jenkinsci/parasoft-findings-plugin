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

import java.util.List;
import java.util.Map;

import hudson.maven.*;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.MavenResultAction;
import hudson.plugins.analysis.core.ParserResult;

public class ParasoftMavenResultAction 
    extends MavenResultAction<ParasoftResult> 
{
    /**
     * Creates a new instance of {@link ParasoftMavenResultAction}.
     *
     * @param owner the associated build of this action
     * @param healthDescriptor health descriptor to use
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the result in this build
     */
    public ParasoftMavenResultAction(AbstractBuild<?, ?> owner, HealthDescriptor healthDescriptor, String defaultEncoding, ParasoftResult result) 
    {
        super(new ParasoftResultAction(owner, healthDescriptor, result), defaultEncoding, ParasoftDescriptor.PLUGIN_ID);
    }

    /**
     * @see hudson.maven.AggregatableAction#createAggregatedAction(hudson.maven.MavenModuleSetBuild, java.util.Map)
     */
    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild build, Map<MavenModule, List<MavenBuild>> moduleBuilds) 
    {
        String sDefaultEncoding = getDefaultEncoding();
        ParasoftResult result = new ParasoftResult(build, sDefaultEncoding, new ParserResult(), false, false);
        return new ParasoftMavenResultAction(build, getHealthDescriptor(), sDefaultEncoding, result);
    }

    /**
     * @see hudson.maven.MavenAggregatedReport#getProjectAction(hudson.maven.MavenModuleSet)
     */
    public Action getProjectAction(MavenModuleSet moduleSet) 
    {
        return new ParasoftProjectAction(moduleSet, ParasoftMavenResultAction.class);
    }

    @Override
    public Class<? extends MavenResultAction<ParasoftResult>> getIndividualActionType() 
    {
        return ParasoftMavenResultAction.class;
    }

    @Override
    protected ParasoftResult createResult(ParasoftResult existingResult, ParasoftResult additionalResult) 
    {
        ParserResult aggregate = aggregate(existingResult, additionalResult);
        String sDefaultEncoding = additionalResult.getDefaultEncoding();

        return new ParasoftReporterResult(getOwner(), sDefaultEncoding, aggregate, existingResult.usePreviousBuildAsStable(), existingResult.useOnlyStableBuildsAsReference());
    }
}

