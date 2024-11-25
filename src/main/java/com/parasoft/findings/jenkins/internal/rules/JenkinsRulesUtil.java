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

package com.parasoft.findings.jenkins.internal.rules;

import java.io.File;
import java.util.Properties;

import com.parasoft.findings.jenkins.util.FilePathUtil;
import com.parasoft.findings.utils.common.util.StringUtil;

import hudson.FilePath;

/**
 * Utility class for operations related to rules.
 */
public class JenkinsRulesUtil
{
    /**
     * Loads settings from remote workspace location
     * @param workspaceDir workspace directory
     * @param settingsPath relative or absolute path to settings
     * @return Parasoft settings
     */
    public static Properties loadSettings(FilePath workspaceDir, String settingsPath)
    {
        if (settingsPath == null || StringUtil.isEmpty(settingsPath)) {
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
