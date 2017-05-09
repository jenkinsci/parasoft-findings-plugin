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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.DataBoundConstructor;

import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;

import hudson.maven.*;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwareReporter;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;

public class ParasoftReporter
    extends HealthAwareReporter<ParasoftResult>
{
    private final String _sReportFilesPattern;

    private final String _sSettingsPath;

    private final boolean _bReportCheckField;

    /**
     * Creates a new instance of <code>ParasoftReporter</code>.
     * 
     * @param healthy Report health as 100% when the number of warnings is less than this value
     * @param unHealthy Report health as 0% when the number of warnings is greater than this
     *        value
     * @param thresholdLimit determines which warning priorities should be considered when
     *        evaluating the build stability and health
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
     * @param canComputeNew determines whether new warnings should be computed (with respect to
     *        baseline)
     * @param reportFilesPattern Ant file-set pattern to scan for Parasoft files
     * @param settingsPath relative path to settings file
     * @param reportCheckField
     */
    @DataBoundConstructor
    public ParasoftReporter(final String healthy, final String unHealthy,
        final String thresholdLimit, final boolean useDeltaValues,
        final String unstableTotalAll, final String unstableTotalHigh,
        final String unstableTotalNormal, final String unstableTotalLow,
        final String unstableNewAll, final String unstableNewHigh,
        final String unstableNewNormal, final String unstableNewLow,
        final String failedTotalAll, final String failedTotalHigh,
        final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
        final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
        final boolean canRunOnFailed, final boolean useStableBuildAsReference,
        final boolean canComputeNew, final String reportFilesPattern, final String settingsPath,
        final boolean reportCheckField)
    {
        super(healthy, unHealthy, thresholdLimit, useDeltaValues, unstableTotalAll,
            unstableTotalHigh, unstableTotalNormal, unstableTotalLow, unstableNewAll,
            unstableNewHigh, unstableNewNormal, unstableNewLow, failedTotalAll,
            failedTotalHigh, failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
            failedNewNormal, failedNewLow, canRunOnFailed, useStableBuildAsReference,
            canComputeNew, PLUGIN_NAME);
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
    protected boolean acceptGoal(final String goal)
    {
        return true;
    }

    @Override
    public ParserResult perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final PluginLogger logger)
        throws InterruptedException, IOException
    {
        logger.log(Messages.COLLECTING_REPORT_FILES);
        
        File file = pom.getBasedir();
        logger.log(file.getAbsolutePath());
        
        PublisherHelper helper = new PublisherHelper(getTargetPath(pom), getSettingsPath(), getReportFilesPattern(), getReportCheckField());

        Logger.getLogger().info("Report location: " + helper.getReportLocation().getRemote()); //$NON-NLS-1$

        ParasoftParser parser = new ParasoftParser( getDefaultEncoding(), helper.getSettings());
        FilesParser parasoftCollector = new FilesParser(PLUGIN_NAME, helper.getReportPattern(), parser, false, true, false);
        ParserResult result = helper.getReportLocation().act(parasoftCollector);
        helper.storeLocalSettings(build.getRootDir());
        
        return result;
    }

    @Override
    protected ParasoftResult createResult(MavenBuild build, ParserResult project)
    {
        return new ParasoftReporterResult(build, getDefaultEncoding(), project,
            usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
    }

    @Override
    protected MavenAggregatedReport createMavenAggregatedReport(MavenBuild build,
        final ParasoftResult result)
    {
        return new ParasoftMavenResultAction(build, this, getDefaultEncoding(), result);
    }

    @Override
    public List<ParasoftProjectAction> getProjectActions(MavenModule module)
    {
        ParasoftProjectAction projectAction = new ParasoftProjectAction(module, getResultActionClass());
        return Collections.singletonList(projectAction);
    }

    @Override
    protected Class<ParasoftMavenResultAction> getResultActionClass()
    {
        return ParasoftMavenResultAction.class;
    }
    
    private static final long serialVersionUID = -3781772412225239385L;

    private static final String PLUGIN_NAME = "PARASOFT"; //$NON-NLS-1$

}
