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

package com.parasoft.xtest.reports.jenkins.views;

import hudson.model.ModelObject;
import hudson.model.Run;

import java.io.File;
import java.io.IOException;

import com.parasoft.xtest.common.IStringConstants;
import com.parasoft.xtest.common.UURL;
import com.parasoft.xtest.common.io.FileUtil;
import com.parasoft.xtest.common.nls.NLS;
import com.parasoft.xtest.common.text.UString;
import com.parasoft.xtest.reports.jenkins.html.IHtmlTags;
import com.parasoft.xtest.reports.jenkins.internal.rules.JenkinsRuleDocumentationProvider;

/**
 * Class represents documentation for a single rule.
 */
public class RuleDocumentation
    implements ModelObject
{
    private final String _sRuleId;

	private final Run<?, ?> _owner;
	
    private final JenkinsRuleDocumentationProvider _docProvider;

    /**
     * @param owner the run context
     * @param sAnalyzer the analyzer associated with the given rule
     * @param sRuleId the rule id
     */
    public RuleDocumentation(Run<?, ?> owner, String sAnalyzer, String sRuleId)
    {
    	_owner = owner;
        File settingsFile = new File(owner.getRootDir(), "localsettings.properties"); //$NON-NLS-1$
        _docProvider = new JenkinsRuleDocumentationProvider(settingsFile, sAnalyzer, sRuleId); 
        _sRuleId = sRuleId;
    }
    
    public Run<?, ?> getOwner()
    {
        return _owner;
    }

    /**
     * @see hudson.model.ModelObject#getDisplayName()
     */
    public String getDisplayName()
    {
        return NLS.getFormatted(Messages.RULE_DOCUMENTATION_DISPLAY_NAME, _sRuleId);
    }

    /**
     * @return url of rule documentation
     */
    public String getRuleDoc()
    {
        String ruleDocLocation = _docProvider.getRuleDocLocation();

        if (UString.isEmpty(ruleDocLocation)) {
            return IHtmlTags.HEADER_START_TAG + Messages.RULE_DOCUMENTATION_UNAVAILABLE + IHtmlTags.HEADER_END_TAG;
        } else if (isLocal(ruleDocLocation)) {
            return getFromLocal(ruleDocLocation);
        }
        return externalHtmlToString(ruleDocLocation);
    }

    private static String getFromLocal(String ruleDocLocation)
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
        return IHtmlTags.HEADER_START_TAG + NLS.getFormatted(Messages.RULE_DOCUMENTATION_UNAVAILABLE_AT,
        		localFile.getAbsolutePath()) + IHtmlTags.HEADER_END_TAG;
    }

    private static boolean isLocal(String ruleDocLocation)
    {
        File localFile = UURL.getLocalFile(UURL.toURL(ruleDocLocation));
        return localFile != null;
    }

    private static String externalHtmlToString(String externalUrl)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<iframe src=\""); //$NON-NLS-1$
        builder.append(externalUrl);
        builder.append("\"style=\"border: 0; position:fixed; top:50; left:100; right:0; bottom:0; width:80%; height:90%\""); //$NON-NLS-1$
        builder.append("</iframe>"); //$NON-NLS-1$ 
        return builder.toString();
    }
}
