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

import hudson.FilePath;
import org.kohsuke.stapler.DataBoundConstructor;

import com.parasoft.xtest.reports.jenkins.internal.variables.JenkinsVariablesResolver;
import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;

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
    
    /**  */
    private final boolean _bReportCheckField;

    /**
     * Creates a new instance of <code>ParasoftPublisher</code>.
     * 
     * @param healthy Report health as 100% when the number of warnings is less than this value
     * @param unHealthy Report health as 0% when the number of warnings is greater than this
     *        value
     * @param thresholdLimit determines which warning priorities should be considered when
     *        evaluating the build stability and health
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param useDeltaValues determines whether the absolute annotations delta or the actual
     *        annotations set difference should be used to evaluate the build stability
     * @param unstableTotalAll annotation threshold
     * @param unstableTotalHigh annotation threshold
     * @param unstableTotalNormal annotation threshold
     * @param unstableTotalLow annotation threshold
     * @param unstableNewAll annotation threshold
     * @param unstableNewHigh annotation threshold
     * @param unstableNewNormal annotation threshold
     * @param unstableNewLow annotation threshold
     * @param failedTotalAll annotation threshold
     * @param failedTotalHigh annotation threshold
     * @param failedTotalNormal annotation threshold
     * @param failedTotalLow annotation threshold
     * @param failedNewAll annotation threshold
     * @param failedNewHigh annotation threshold
     * @param failedNewNormal annotation threshold
     * @param failedNewLow annotation threshold
     * @param canRunOnFailed determines whether the plug-in can run for failed builds, too
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *        reference builds or not
     * @param shouldDetectModules determines whether module names should be derived from Maven
     *        POM or Ant build files
     * @param canComputeNew determines whether new warnings should be computed (with respect to
     *        baseline)
     * @param reportFilesPattern Ant file-set pattern to scan for Parasoft files
     * @param settingsPath relative path to the settings file
     * @param reportCheckField
     */
    @DataBoundConstructor
    public ParasoftPublisher(   String healthy, String unHealthy,
                                String thresholdLimit, String defaultEncoding,
                                boolean useDeltaValues, String unstableTotalAll,
                                String unstableTotalHigh, String unstableTotalNormal,
                                String unstableTotalLow, String unstableNewAll,
                                String unstableNewHigh, String unstableNewNormal,
                                String unstableNewLow, String failedTotalAll, String failedTotalHigh,
                                String failedTotalNormal, String failedTotalLow, String failedNewAll,
                                String failedNewHigh, String failedNewNormal, String failedNewLow,
                                boolean canRunOnFailed, boolean useStableBuildAsReference,
                                boolean shouldDetectModules, boolean canComputeNew, String reportFilesPattern,
                                String settingsPath, boolean reportCheckField)
    {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
            unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
            unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow, failedTotalAll,
            failedTotalHigh, failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
            failedNewNormal, failedNewLow, canRunOnFailed, useStableBuildAsReference,
            shouldDetectModules, canComputeNew, false, PLUGIN_NAME);
        _sReportFilesPattern = reportFilesPattern;
        _sSettingsPath = settingsPath;
        _bReportCheckField = reportCheckField;
    }
    
    /**
     * @return path to custom settings, if present in project configuration.
     */
    public String getSettingsPath()
    {
        return _sSettingsPath;
    }

    /**
     * Returns the Ant file-set pattern of files containing xml reports to work with.
     * 
     * @return Ant file-set pattern of report files to work with
     */
    public String getReportFilesPattern()
    {
        return _sReportFilesPattern;
    }
    
    /**
     * Returns whether custom patterns for report files are enabled.
     * 
     * @return true if project configuration enables custom patterns for report files.
     */
    public boolean getReportCheckField()
    {
        return _bReportCheckField;
    }

    @Override
    public BuildResult perform(final Run<?, ?> build, FilePath workspace, final PluginLogger logger)
        throws InterruptedException, IOException
    {
        logger.log(Messages.COLLECTING_REPORT_FILES);
        
        //Map<String, String> buildVars = ((AbstractBuild)build).getBuildVariables();
        Map<String, String> envVars = build.getCharacteristicEnvVars();
        //envVars.putAll(buildVars);
        
        JenkinsVariablesResolver variablesResolver = new JenkinsVariablesResolver(envVars);

        boolean bReportCheckField = getReportCheckField();
        String sReportPattern = variablesResolver.performSubstitution(getReportFilesPattern());
        String sSettingsPath = variablesResolver.performSubstitution(getSettingsPath());
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
        build.getActions().add(new ParasoftResultAction(build, this, result));

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
