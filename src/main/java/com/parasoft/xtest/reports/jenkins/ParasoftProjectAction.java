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

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Entry point to visualize the Parasoft trend graph in the project screen. Drawing
 * of the graph is delegated to the associated {@link ResultAction}.
 */
public class ParasoftProjectAction
    extends AbstractProjectAction<ResultAction<ParasoftResult>>
{
    /**
     * Instantiates a new {@link ParasoftProjectAction}.
     *
     * @param project the project that owns this action
     */
    public ParasoftProjectAction(AbstractProject<?, ?> project)
    {
        this(project, ParasoftResultAction.class);
    }

    /**
     * Instantiates a new {@link ParasoftProjectAction}.
     *
     * @param project the project that owns this action
     * @param type the result action type
     */
    public ParasoftProjectAction(AbstractProject<?, ?> project, Class<? extends ResultAction<ParasoftResult>> type)
    {
        super(project, type, new LocalizableString(Messages.PARASOFT_PROJECT_ACTION_NAME), new LocalizableString(Messages.PARASOFT_TREND_NAME),
            ParasoftDescriptor.PLUGIN_ID, ParasoftDescriptor.ICON_URL, ParasoftDescriptor.RESULT_URL);
    }
}

