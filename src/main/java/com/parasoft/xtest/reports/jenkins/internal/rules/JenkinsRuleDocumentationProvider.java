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

import java.io.*;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.parasoft.xtest.common.dtp.IDtpServiceRegistry;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.configuration.rules.RuleDocumentationHelper;
import com.parasoft.xtest.configuration.rules.XRestRulesClient;
import com.parasoft.xtest.services.api.IParasoftServiceContext;
import com.parasoft.xtest.services.api.ServiceUtil;

/** 
 * Provides information about location of rule docs.
 */
public class JenkinsRuleDocumentationProvider
{
    private final RuleDocumentationHelper _ruleDocHelper;

    /**
     * @param settingsFile the settings to configure access to the documentation
     * @param analyzer the analyzer connected with the given rule
     * @param ruleId the rule identifier
     */
    public JenkinsRuleDocumentationProvider(File settingsFile, String analyzer, String ruleId)
    {
        Properties settings = loadLocalSettings(settingsFile); 
        IParasoftServiceContext context = new RawServiceContext(settings);
        XRestRulesClient client = getRuleClient(context);
        _ruleDocHelper = new RuleDocumentationHelper(ruleId, analyzer, client, context);
    }
    
    /**
     * @return url of rule docs or null
     */
    public String getRuleDocLocation()
    {
        return _ruleDocHelper.getRuleDocLocation();
    }

    private static XRestRulesClient getRuleClient(IParasoftServiceContext context)
    {
        IDtpServiceRegistry registry = ServiceUtil.getService(IDtpServiceRegistry.class, context);
        if (registry == null) {
            return null;
        }

        XRestRulesClient rulesClient = XRestRulesClient.create(registry);
        if (rulesClient == null) {
            Logger.getLogger().info("Rules service client could not be created."); //$NON-NLS-1$
        }
        return rulesClient;
    }

    private static Properties loadLocalSettings(File file)
    {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (FileNotFoundException e) {
            Logger.getLogger().warn("Localsettings file not found", e); //$NON-NLS-1$
        } catch (IOException e) {
            Logger.getLogger().warnTrace(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
        return new Properties();
    }

}
