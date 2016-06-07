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
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;

import java.util.Collection;

public class ParasoftTabDetail
    extends TabDetail
{

    public ParasoftTabDetail(AbstractBuild<?, ?> owner, DetailFactory detailFactory,
        Collection<FileAnnotation> annotations, String url, String defaultEncoding)
    {
        super(owner, detailFactory, annotations, url, defaultEncoding);
    }
    
    @Override
    public String getWarnings() 
    {
        return PARASOFT_WARNINGS_JELLY;  
    }

    @Override
    public String getDetails() 
    {
        return PARASOFT_DETAILS_JELLY;  
    }
    
    private static final long serialVersionUID = -862952398133090156L;
    
    private static final String PARASOFT_DETAILS_JELLY = "parasoft-details.jelly"; //$NON-NLS-1$
    private static final String PARASOFT_WARNINGS_JELLY = "parasoft-warnings.jelly"; //$NON-NLS-1$
}
