/*
 * Copyright 2016 Parasoft Corporation
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
