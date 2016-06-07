/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.parasoft.xtest.reports.jenkins.internal.variables.VariablePatternVerifier;

/**
 * Descriptor for the class {@link ParasoftPublisher}. Used as a singleton. 
 * The class is marked as public so it can be accessed from views.
 */
@Extension(ordinal = 100)
public final class ParasoftDescriptor
    extends PluginDescriptor
{
    public ParasoftDescriptor()
    {
        super(ParasoftPublisher.class);
    }

    @Override
    public String getDisplayName()
    {
        return Messages.PARASOFT_PUBLISHER_NAME;
    }

    @Override
    public String getPluginName()
    {
        return PLUGIN_ID;
    }

    @Override
    public String getIconUrl()
    {
        return ICON_URL;
    }

    @Override
    public String getSummaryIconUrl()
    {
        return ICONS_PREFIX + "parasofttest48.png"; //$NON-NLS-1$
    }

    @Override
    public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String pattern)
        throws IOException
    {
        VariablePatternVerifier patternVerifier = new VariablePatternVerifier(pattern);
        if (patternVerifier.containsVariables()) {
            if (patternVerifier.checkVariableNotation()) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(patternVerifier.getErrorMessage());
            }
        }
        return super.doCheckPattern(project, pattern);
    }
    
    private static final String ICONS_PREFIX = "/plugin/com.parasoft.xtest.reports.jenkins/icons/"; //$NON-NLS-1$

    /** The ID of this plug-in is used as URL. */
    public static final String PLUGIN_ID = "com.parasoft.xtest.reports.jenkins"; //$NON-NLS-1$

    /** The URL of the result action. */
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);

    /** Icon to use for the result and project action. */
    static final String ICON_URL = ICONS_PREFIX + "parasofttest24.png"; //$NON-NLS-1$

}