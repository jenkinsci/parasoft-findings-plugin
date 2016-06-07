/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.SourceDetail;
import hudson.plugins.analysis.views.TabDetail;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.parasoft.xtest.reports.jenkins.parser.Warning;
import com.parasoft.xtest.reports.jenkins.views.RuleDocumentation;

public class ParasoftDetailBuilder
    extends DetailFactory
{

    @Override
    public Object createDetails(String link, AbstractBuild<?, ?> owner,
        AnnotationContainer container, String defaultEncoding, String displayName)
    {
        if (link.startsWith("link.")) {  //$NON-NLS-1$
            String suffix = StringUtils.substringAfter(link, "link.");  //$NON-NLS-1$
            String[] fromToStrings = StringUtils.split(suffix, "."); //$NON-NLS-1$
            if (fromToStrings.length == 2) {
                return createParasoftSourceDetail(owner, container, defaultEncoding, fromToStrings[0], fromToStrings[1]);
            }
            return null;
        }
        if (link.startsWith("doc.")) { //$NON-NLS-1$
            String sAnalzerRuleId = StringUtils.substringAfter(link, "doc."); //$NON-NLS-1$
            String[] splitted = sAnalzerRuleId.split("\\|"); //$NON-NLS-1$
            if (splitted.length != 2) {
                Logger.getLogger().warn("Wrong url with analyzer and ruleId, cannot parse: " + sAnalzerRuleId); //$NON-NLS-1$
                return null;
            }
            return new RuleDocumentation(owner, splitted[0], splitted[1]);
        }
        return super.createDetails(link, owner, container, defaultEncoding, displayName);
    }
    
    @Override
    protected TabDetail createTabDetail(AbstractBuild<?, ?> owner,
                                        Collection<FileAnnotation> annotations, String url, String defaultEncoding)
    {
        return new ParasoftTabDetail(owner, this, annotations, url, defaultEncoding);
    }
  

    /**
     * Creates the dry source detail view.
     *
     * @param owner the owner
     * @param container the container
     * @param sDefaultEncoding the default encoding
     * @param sFromString from hash
     * @param sToString to hash
     * @return the detail view or <code>null</code>
     */
    private static Object createParasoftSourceDetail(   AbstractBuild<?, ?> owner, AnnotationContainer container, 
                                                        String sDefaultEncoding, String sFromString, String sToString) 
    {
        long from = Long.parseLong(sFromString);
        long to = Long.parseLong(sToString);

        FileAnnotation fromAnnotation = container.getAnnotation(from);
        if (fromAnnotation instanceof Warning) {
            return new SourceDetail(owner, ((Warning)fromAnnotation).getPreviousCall(to), sDefaultEncoding);
        }
        return null;
    }

}
