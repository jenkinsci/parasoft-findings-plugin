/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.xtest.reports.jenkins.xunit;

import hudson.Extension;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

/** 
 * Identifies our tests.
 */
public class ParasoftType 
    extends TestType 
{

    private static final long serialVersionUID = -636297282706314455L;

    @DataBoundConstructor
    public ParasoftType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) 
    {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class ParasoftTypeDescriptor extends TestTypeDescriptor<ParasoftType> {

        public ParasoftTypeDescriptor() 
        {
            super(ParasoftType.class, ParasoftInputMetric.class);
        }

    }

}
