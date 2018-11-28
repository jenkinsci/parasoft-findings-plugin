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

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.parasoft.xtest.reports.jenkins.internal.variables.VariablePatternVerifier;

/**
 * Descriptor for the class {@link ParasoftPublisher}. Used as a singleton. 
 * The class is marked as public so it can be accessed from views.
 */
@Symbol("parasoftFindings")
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
    
    private static final String ICONS_PREFIX = "/plugin/parasoft-findings/icons/"; //$NON-NLS-1$

    /** The ID of this plug-in is used as URL. */
    public static final String PLUGIN_ID = "parasoft-findings"; //$NON-NLS-1$

    /** The URL of the result action. */
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);

    /** Icon to use for the result and project action. */
    static final String ICON_URL = ICONS_PREFIX + "parasofttest24.png"; //$NON-NLS-1$

}