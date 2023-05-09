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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.common.io.FileUtil;
import com.parasoft.xtest.common.text.UString;

public class RuleDocumentationReader
{
    private File _buildRoot = null;
    private String _rulesDocDir = null;

    private Map<String, String> _ruleDocMap = null;

    public RuleDocumentationReader(File buildRoot)
    {
        this(buildRoot, DEFAULT_RULES_DIR);
    }

    public RuleDocumentationReader(File buildRoot, String rulesDocDir)
    {
        _buildRoot = buildRoot;
        _rulesDocDir = rulesDocDir;
        _ruleDocMap = new HashMap<>();
    }

    public String getRuleDoc(String analyzer, String ruleId)
    {
        String key = getRuleDocKey(analyzer, ruleId);
        String contents = _ruleDocMap.get(key);
        if (UString.isNonEmpty(contents)) {
            return contents;
        }
        File ruleDoc = new File(_buildRoot, getRuleDocRelativePath(_rulesDocDir, analyzer, ruleId));
        if (ruleDoc.exists()) {
            try {
                contents = FileUtil.readFile(ruleDoc, IStringConstants.UTF_8);
            } catch (IOException e) {
                Logger.getLogger().error(e);
            }
        }
        contents = contents != null ? contents : IStringConstants.EMPTY; //Messages.RULE_DOCUMENTATION_UNAVAILABLE;
        _ruleDocMap.put(key, contents);
        return contents;
    }

    public static String getRuleDocRelativePath(String rulesDocDir, String analyzer, String ruleId)
    {
        return rulesDocDir + '/' + analyzer + '/' + ruleId + ".html"; //$NON-NLS-1$
    }
    
    public static String getRuleDocKey(String analyzer, String ruleId)
    {
        return analyzer + '_' + ruleId;
    }
    
    static final String DEFAULT_RULES_DIR = "parasoft-findings-rules"; //$NON-NLS-1$
}
