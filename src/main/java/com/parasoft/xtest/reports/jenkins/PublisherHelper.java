/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import java.io.*;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.api.ILocalSettingsConstants;
import com.parasoft.xtest.reports.jenkins.util.FilePathUtil;

public class PublisherHelper
{
    private final FilePath _workspaceDir;
    private final Properties _settings;
    private final String _sSettingsPath;
    private final String _sReportPattern;
    private final boolean _bReportCheckField;

    /**
     * @param workspaceDir the workspace
     * @param sSettingsPath path to the settings file, could be relative to current workspace
     * @param sReportPattern pattern to use when matching report files
     * @param bReportCheckField whether custom report matching pattern should be used
     */
    public PublisherHelper(FilePath workspaceDir, String sSettingsPath, String sReportPattern, boolean bReportCheckField)
    {
        super();
        _workspaceDir = workspaceDir;
        _sSettingsPath = sSettingsPath;
        _settings = loadSettings();
        _sReportPattern = sReportPattern;
        _bReportCheckField = bReportCheckField;
    }
    
    /**
     * Provides pattern to use when looking for report files.
     * @return the pattern to use
     */
    public String getReportPattern()
    {
        String sReportLocationName = _settings.getProperty(ILocalSettingsConstants.REPORT_LOCATION);
        String sPattern = DEFAULT_REPORT_FILES_PATTERN;
        if (_bReportCheckField && UString.isNonEmpty(_sReportPattern)) {
            sPattern = _sReportPattern;
        }
        if (!_bReportCheckField && UString.isNonEmpty(sReportLocationName)) {
            sPattern = REPORT_FILE_NAME;
        }
        return sPattern;
    }

    /**
     * Provides the path to the report file.
     * @return path where report should be placed
     */
    public FilePath getReportLocation()
    {
        String reportLocationName = _settings.getProperty(ILocalSettingsConstants.REPORT_LOCATION);
        FilePath reportLocation = _workspaceDir;
        if (!_bReportCheckField && UString.isNonEmpty(reportLocationName)) {
            reportLocation = new FilePath(_workspaceDir.getChannel(), reportLocationName);
            if (!FilePathUtil.isAbsolute(reportLocation)) {
                reportLocation = new FilePath(_workspaceDir, reportLocationName);
            }
        }
        return reportLocation;
    }

    /**
     * Provides currently used settings.
     * @return all settings
     */
    public Properties getSettings()
    {
        return _settings;
    }

    /**
     * Saves settings in a file in given target directory.
     * @param rootDir the directory where to place settings
     */
    public void storeLocalSettings(File rootDir)
    {
        PrintWriter writer = null;
        OutputStream outputStream = null;
        try {
            File file = new File(rootDir, SETTINGS_FILE_NAME);
            outputStream = new FileOutputStream(file);
            _settings.store(outputStream, "Parasoft localsettings"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            Logger.getLogger().warn("Localsettings file not found", e); //$NON-NLS-1$
        } catch (IOException e) {
            Logger.getLogger().warnTrace(e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(outputStream);
        }

    }
    
    /**
     * Saves settings in a file in given target directory.
     * @param rootDir the directory where to place settings
     */
    public void storeLocalSettings(FilePath rootDir)
    {
        try{
        rootDir.act(new FileCallable<Boolean>()
        {
            private static final long serialVersionUID = 1L;

            public Boolean invoke(File file, VirtualChannel channel)
                throws IOException, InterruptedException
            {
                storeLocalSettings(file);
                return Boolean.TRUE;
            }});
        } catch (IOException e) {
            Logger.getLogger().errorTrace(e);
        } catch (InterruptedException e) {
            Logger.getLogger().errorTrace(e);
        }
    }
    
    /**
     * Replace current settings with given ones.
     * @param settings the settings to use
     */
    public void setLocalSettings(Properties settings)
    {
        _settings.clear();
        _settings.putAll(settings);
    }
    

    private Properties loadSettings()
    {
        if (UString.isEmpty(_sSettingsPath)) {
            return new Properties();
        }
        FilePath localSettingsFile = new FilePath(_workspaceDir, _sSettingsPath);
        Logger.getLogger().info("Path to local settings is " + localSettingsFile.getRemote());   //$NON-NLS-1$
        return FilePathUtil.loadProperties(localSettingsFile);
    }
    
    /** Default report file name. */
    static final String REPORT_FILE_NAME = "report.xml"; //$NON-NLS-1$
    
    /** Default pattern. */
    static final String DEFAULT_REPORT_FILES_PATTERN = "**/" + REPORT_FILE_NAME;  //$NON-NLS-1$
    
    /** Default name for settings file. */
    static final String SETTINGS_FILE_NAME = "localsettings.properties";  //$NON-NLS-1$

}

