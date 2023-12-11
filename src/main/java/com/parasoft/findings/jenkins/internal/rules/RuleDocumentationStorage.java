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

package com.parasoft.findings.jenkins.internal.rules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.parasoft.findings.utils.common.util.StringUtil;
import com.parasoft.findings.utils.common.util.URLUtil;
import com.parasoft.findings.utils.doc.RuleDocumentationProvider;
import org.jenkinsci.remoting.RoleChecker;

import com.parasoft.findings.utils.common.IStringConstants;
import com.parasoft.findings.utils.common.util.FileUtil;
import com.parasoft.findings.utils.common.util.IOUtils;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

public class RuleDocumentationStorage
{
    private File _buildRoot = null;

    private String _rulesDocDir = null;

    private Set<String> _ruleDocs = null;

    private final RuleDocumentationProvider _docProvider;

    public RuleDocumentationStorage(File buildRoot, Properties settings)
    {
        this(buildRoot, RuleDocumentationReader.DEFAULT_RULES_DIR, settings);
    }

    public RuleDocumentationStorage(File buildRoot, String rulesDocDir, Properties settings)
    {
        _buildRoot = buildRoot;
        _ruleDocs = new HashSet<>();
        _rulesDocDir = rulesDocDir;
        _docProvider = new RuleDocumentationProvider(settings);
    }

    public void storeRuleDoc(String analyzer, String ruleId)
    {
        String key = RuleDocumentationReader.getRuleDocKey(analyzer, ruleId);
        if (_ruleDocs.contains(key)) {
            return;
        }
        String ruleDocLocation = _docProvider.getRuleDocLocation(analyzer, ruleId);

        String contents = null;
        if (StringUtil.isNonEmpty(ruleDocLocation)) {
            if (isLocal(ruleDocLocation)) {
                contents = readFromLocal(ruleDocLocation);
            } else {
                contents = readExternalURL(ruleDocLocation);
            }
        }
        if (StringUtil.isNonEmptyTrimmed(contents)) {
            storeRuleDoc(new FilePath(_buildRoot), _rulesDocDir, analyzer, ruleId, contents);
        }
        _ruleDocs.add(key);
    }

    private String readFromLocal(String ruleDocLocation)
    {
        File localFile = URLUtil.getLocalFile(URLUtil.toURL(ruleDocLocation));
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

    private boolean isLocal(String ruleDocLocation)
    {
        File localFile = URLUtil.getLocalFile(URLUtil.toURL(ruleDocLocation));
        return localFile != null;
    }

    private String readExternalURL(String externalUrl)
    {
        return this._docProvider.getDtpRuleDocContent(externalUrl);
    }

    private void storeRuleDoc(FilePath rootDir, String ruleDocDir, String analyzer, String ruleId, String contents)
    {
        try {
            rootDir.act(new InternalStoreRuleDocFileCallable(ruleDocDir, analyzer, ruleId, contents));
        } catch (IOException e) {
            Logger.getLogger().errorTrace(e);
        } catch (InterruptedException e) {
            Logger.getLogger().errorTrace(e);
        }
    }

    private static final class InternalStoreRuleDocFileCallable implements FileCallable<Boolean> {

        private static final long serialVersionUID = 1L;

        private final String ruleDocDir;
        private final String analyzer;
        private final String ruleId;
        private final String contents;

        InternalStoreRuleDocFileCallable(String ruleDocDir, String analyzer, String ruleId, String contents) {
            this.ruleDocDir = ruleDocDir;
            this.analyzer = analyzer;
            this.ruleId = ruleId;
            this.contents = contents;
        }

        @Override
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
    }

    private static void internalStoreRuleDoc(File rootDir, String ruleDocFile, String contents)
    {
        Writer writer = null;
        try {
            File file = new File(rootDir, ruleDocFile);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                if (parent.mkdirs()){
                    Logger.getLogger().debug("Created rules dir: " + parent.getAbsolutePath()); //$NON-NLS-1$
                }
            }
            writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), IStringConstants.UTF_8); // parasoft-suppress BD.RES.LEAKS "Closed in finally"
            writer.write(contents);
        } catch (IOException e) {
            Logger.getLogger().warnTrace(e);
        } finally {
            IOUtils.close(writer);
        }
    }
}
