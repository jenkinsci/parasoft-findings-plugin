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

package com.parasoft.xtest.reports.jenkins.internal.rules;

import java.io.File;
import java.util.Properties;

import com.parasoft.xtest.common.api.progress.EmptyProgressMonitor;
import com.parasoft.xtest.common.api.progress.IProgressMonitor;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.configuration.api.ConfigurationException;
import com.parasoft.xtest.configuration.api.rules.IRuleDescriptionUpdateService;
import com.parasoft.xtest.reports.jenkins.util.FilePathUtil;
import com.parasoft.xtest.services.api.IParasoftServiceContext;
import com.parasoft.xtest.services.api.ServiceUtil;

import hudson.FilePath;

/**
 * Utility class for operations related to rules.
 */
public class JenkinsRulesUtil
{
    /**
     * Refreshes rules information if rule providers have non-static rule descriptions and are capable of update during runtime.
     * This requires given settings to contain complete information required to perform such update. 
     * 
     * @param settings settings to use to refresh rules
     */
    public static void refreshRuleDescriptions(Properties settings)
    {
        IParasoftServiceContext context = new RawServiceContext(settings);
        
        IProgressMonitor monitor = EmptyProgressMonitor.getInstance();
        IRuleDescriptionUpdateService ruleDescriptionUpdateService = ServiceUtil.getService(IRuleDescriptionUpdateService.class);
        if (ruleDescriptionUpdateService != null) {
            try {
                ruleDescriptionUpdateService.refreshSharedDescriptions(context, monitor.subTask(1));
            } catch (ConfigurationException ce) {
                Logger.getLogger().error(ce);
            }
        }
    }

    /**
     * Loads settings from remote workspace location
     * @param workspaceDir workspace directory
     * @param settingsPath relative or absolute path to settings
     * @return Parasoft settings
     */
    public static Properties loadSettings(FilePath workspaceDir, String settingsPath)
    {
        if (UString.isEmpty(settingsPath)) {
            return new Properties();
        }
        FilePath localSettingsFile = null;
        File localPath = new File(settingsPath);
        if (localPath.isAbsolute() && localPath.exists()) {
            localSettingsFile = new FilePath(localPath);
        } else {
            localSettingsFile = new FilePath(workspaceDir, settingsPath);
        }
        Logger.getLogger().info("Path to local settings is " + localSettingsFile.getRemote()); //$NON-NLS-1$
        return FilePathUtil.loadProperties(localSettingsFile);
    }
}
