/*
 * $Id$
 * 
 * (C) Copyright Parasoft Corporation 2013. All rights reserved. THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft The copyright notice above
 * does not evidence any actual or intended publication of such source code.
 */

package com.parasoft.xtest.reports.jenkins.views;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;

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

	private final AbstractBuild<?, ?> _owner;

    private final JenkinsRuleDocumentationProvider _docProvider;

    /**
     * @param owner
     * @param sAnalyzer
     * @param sRuleId
     */
    public RuleDocumentation(AbstractBuild<?, ?> owner, String sAnalyzer, String sRuleId)
    {
    	_owner = owner;
        File settingsFile = new File(owner.getRootDir(), "localsettings.properties"); //$NON-NLS-1$
        _docProvider = new JenkinsRuleDocumentationProvider(settingsFile, sAnalyzer, sRuleId); 
        _sRuleId = sRuleId;
    }
    
    public AbstractBuild<?, ?> getOwner()
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
        try {
            return FileUtil.readFile(localFile);
        } catch (IOException e) {
            Logger.getLogger().error(e);
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
