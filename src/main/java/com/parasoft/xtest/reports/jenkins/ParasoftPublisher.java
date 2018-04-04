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

import java.io.IOException;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

import com.parasoft.xtest.reports.jenkins.internal.variables.JenkinsVariablesResolver;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.*;
import hudson.plugins.analysis.core.*;
import hudson.plugins.analysis.util.PluginLogger;

/**
 * Publishes the results of the Parasoft analysis (collected during execution of a "freestyle project").
 */
public class ParasoftPublisher
    extends HealthAwarePublisher
{
    /** Ant file-set pattern of report files to work with. */
    private final String _sReportFilesPattern;
    
    /** Path to non-default settings file */
    private final String _sSettingsPath;
    
    /** If pattern should be applied */
    private boolean _bReportCheckField;

    /**
     * Creates a new instance of <code>ParasoftPublisher</code>.
     * 
     * @param reportPattern Ant file-set pattern to scan for Parasoft files
     * @param settings relative path to the settings file
     * @param useReportPattern if <tt>true</tt> then report pattern will be used to find report files
     */
    @DataBoundConstructor
    public ParasoftPublisher(String reportPattern, String settings, boolean useReportPattern)
    {
        super(PLUGIN_NAME);
        _sReportFilesPattern = reportPattern;
        _sSettingsPath = settings;
        _bReportCheckField = useReportPattern;
    }
    
    /**
     * @return path to custom settings, if present in project configuration.
     */
    public String getSettings()
    {
        return _sSettingsPath;
    }

    /**
     * Returns the Ant file-set pattern of files containing xml reports to work with.
     * 
     * @return Ant file-set pattern of report files to work with
     */
    public String getReportPattern()
    {
        return _sReportFilesPattern;
    }
    
    /**
     * Returns whether custom patterns for report files are enabled.
     * 
     * @return true if project configuration enables custom patterns for report files.
     */
    public boolean getUseReportPattern()
    {
        return _bReportCheckField;
    }

    @Override
    public BuildResult perform(final Run<?, ?> build, FilePath workspace, final PluginLogger logger)
        throws InterruptedException, IOException
    {
        logger.log(Messages.COLLECTING_REPORT_FILES);
        
        Map<String, String> envVars = build.getEnvironment(TaskListener.NULL);
        
        JenkinsVariablesResolver variablesResolver = new JenkinsVariablesResolver(envVars);

        boolean bReportCheckField = getUseReportPattern();
        String sReportPattern = variablesResolver.performSubstitution(getReportPattern());
        String sSettingsPath = variablesResolver.performSubstitution(getSettings());
        PublisherHelper helper = new PublisherHelper(workspace, sSettingsPath, sReportPattern, bReportCheckField);

        Logger.getLogger().info("Using report files location: " + helper.getReportLocation().getRemote()); //$NON-NLS-1$

        boolean bMavenBuild = isMavenBuild(build);
        boolean bDetectModules = shouldDetectModules();
        ParasoftParser parser = new ParasoftParser(getDefaultEncoding(), helper.getSettings());
        FilesParser parasoftCollector = new FilesParser(PLUGIN_NAME, helper.getReportPattern(), parser, bDetectModules, bMavenBuild, false);
        ParserResult project = helper.getReportLocation().act(parasoftCollector);
        logger.logLines(project.getLogMessages());
        helper.storeLocalSettings(build.getRootDir());

        ParasoftResult result = new ParasoftResult(build, getDefaultEncoding(), project,
            usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
        build.addAction(new ParasoftResultAction(build, this, result));

        return result;
    }

    @Override
    public ParasoftDescriptor getDescriptor()
    {
        return (ParasoftDescriptor)super.getDescriptor();
    }

    /**
     * @see hudson.matrix.MatrixAggregatable#createAggregator(hudson.matrix.MatrixBuild, hudson.Launcher, hudson.model.BuildListener)
     */
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
        final BuildListener listener)
    {
        return new ParasoftAnnotationsAggregator(build, launcher, listener, this,
            getDefaultEncoding(), usePreviousBuildAsReference(),
            useOnlyStableBuildsAsReference());
    }
    

    private static final long serialVersionUID = 1212746148354943794L;

    private static final String PLUGIN_NAME = "PARASOFT";  //$NON-NLS-1$

}
