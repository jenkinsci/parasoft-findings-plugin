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

import hudson.model.Run;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;

public class ParasoftReporterResult 
    extends ParasoftResult 
{

    /**
     * Creates a new instance of {@link ParasoftReporterResult}.
     *
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param usePreviousBuildAsReference determines whether previous builds should be used as reference builds or not
     * @param useStableBuildAsReference determines whether only stable builds should be used as reference builds or not
     */
    public ParasoftReporterResult(Run<?, ?> build, String defaultEncoding, ParserResult result, boolean usePreviousBuildAsReference, boolean useStableBuildAsReference) 
    {
        super(build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference, ParasoftMavenResultAction.class);
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() 
    {
        return ParasoftMavenResultAction.class;
    }
    
    private static final long serialVersionUID = -6141604013001874832L;
}

