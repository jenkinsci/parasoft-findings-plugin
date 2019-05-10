/*
 * Copyright 2019 Parasoft Corporation
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.jenkinsci.remoting.RoleChecker;

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.common.UURL;
import com.parasoft.xtest.common.dtp.IDtpServiceRegistry;
import com.parasoft.xtest.common.io.FileUtil;
import com.parasoft.xtest.common.io.IOUtils;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.configuration.dtp.XRestRulesClient;
import com.parasoft.xtest.configuration.rules.RuleDocumentationHelper;
import com.parasoft.xtest.services.api.IParasoftServiceContext;
import com.parasoft.xtest.services.api.ServiceUtil;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

public class RuleDocumentationStorage
{
    private File _buildRoot = null;

    private String _rulesDocDir = null;

    private Set<String> _ruleDocs = null;

    private final JenkinsRuleDocumentationProvider _docProvider;

    public RuleDocumentationStorage(File buildRoot, Properties settings)
    {
        this(buildRoot, RuleDocumentationReader.DEFAULT_RULES_DIR, settings);
    }

    public RuleDocumentationStorage(File buildRoot, String rulesDocDir, Properties settings)
    {
        _buildRoot = buildRoot;
        _ruleDocs = new HashSet<>();
        _rulesDocDir = rulesDocDir;
        _docProvider = new JenkinsRuleDocumentationProvider(settings);
    }

    public void storeRuleDoc(String analyzer, String ruleId)
    {
        String key = RuleDocumentationReader.getRuleDocKey(analyzer, ruleId);
        if (_ruleDocs.contains(key)) {
            return;
        }
        String ruleDocLocation = _docProvider.getRuleDocLocation(analyzer, ruleId);

        String contents = null;
        if (UString.isNonEmpty(ruleDocLocation)) {
            if (isLocal(ruleDocLocation)) {
                contents = readFromLocal(ruleDocLocation);
            } else {
                contents = readExternalURL(ruleDocLocation);
            }
        }
        if (UString.isNonEmptyTrimmed(contents)) {
            storeRuleDoc(new FilePath(_buildRoot), _rulesDocDir, analyzer, ruleId, contents);
        }
        _ruleDocs.add(key);
    }

    private static String readFromLocal(String ruleDocLocation)
    {
        File localFile = UURL.getLocalFile(UURL.toURL(ruleDocLocation));
        if (localFile != null) {
            try {
                return FileUtil.readFile(localFile, IStringConstants.UTF_8);
            } catch (IOException e) {
                Logger.getLogger().error(e);
            }
        } else {
            Logger.getLogger().error("Failed to determine local file for rule doc location: " + ruleDocLocation); //$NON-NLS-1$
        }
        return IStringConstants.EMPTY;
    }

    private static boolean isLocal(String ruleDocLocation)
    {
        File localFile = UURL.getLocalFile(UURL.toURL(ruleDocLocation));
        return localFile != null;
    }

    private static String readExternalURL(String externalUrl)
    {
        StringBuilder builder = new StringBuilder();
        BufferedReader in = null;
        try {
            URL ruleDocUrl = new URL(externalUrl);
            in = new BufferedReader(new InputStreamReader(ruleDocUrl.openStream(), IStringConstants.UTF_8));

            return FileUtil.readFile(in);
        } catch (IOException ioe) {} finally {
            IOUtils.close(in);
        }
        return builder.toString();
    }

    private void storeRuleDoc(FilePath rootDir, String ruleDocDir, String analyzer, String ruleId, String contents)
    {
        try {
            rootDir.act(new FileCallable<Boolean>()
            {
                private static final long serialVersionUID = 1L;

                public Boolean invoke(File file, VirtualChannel channel)
                    throws IOException, InterruptedException
                {
                    internalStoreRuleDoc(file, RuleDocumentationReader.getRuleDocRelativePath(ruleDocDir, analyzer, ruleId), contents);
                    return Boolean.TRUE;
                }

                @Override
                public void checkRoles(RoleChecker arg0)
                    throws SecurityException
                {}
            });
        } catch (IOException e) {
            Logger.getLogger().errorTrace(e);
        } catch (InterruptedException e) {
            Logger.getLogger().errorTrace(e);
        }
    }

    private void internalStoreRuleDoc(File rootDir, String ruleDocFile, String contents)
    {
        Writer writer = null;
        try {
            File file = new File(rootDir, ruleDocFile);
            if (file.getParentFile().mkdirs()) {
                writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), IStringConstants.UTF_8);
                writer.write(contents);
            }
        } catch (IOException e) {
            Logger.getLogger().warnTrace(e);
        } finally {
            IOUtils.close(writer);
        }
    }

    private static class JenkinsRuleDocumentationProvider
    {
        private IParasoftServiceContext _context;

        private XRestRulesClient _client;

        /**
         * @param settingsFile the settings to configure access to the documentation
         * @param analyzer the analyzer connected with the given rule
         * @param ruleId the rule identifier
         */
        public JenkinsRuleDocumentationProvider(Properties settings)
        {
            _context = new RawServiceContext(settings);
            _client = getRuleClient(_context);
        }
        
        /**
         * @return url of rule docs or null
         */
        public String getRuleDocLocation(String analyzer, String ruleId)
        {
            RuleDocumentationHelper ruleDocHelper = new RuleDocumentationHelper(ruleId, analyzer, _client, _context);
            return ruleDocHelper.getRuleDocLocation();
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
    }
}
