/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.Extension;
import hudson.plugins.analysis.core.ReporterDescriptor;

@Extension(ordinal = 100, optional = true)
public class ParasoftReporterDescriptor 
    extends ReporterDescriptor 
{
    /**
     * Creates a new instance of <code>ParasoftReporterDescriptor</code>.
     */
    public ParasoftReporterDescriptor() 
    {
        super(ParasoftReporter.class, new ParasoftDescriptor());
    }
}

