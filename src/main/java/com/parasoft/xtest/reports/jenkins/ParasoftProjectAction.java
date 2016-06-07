/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
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

