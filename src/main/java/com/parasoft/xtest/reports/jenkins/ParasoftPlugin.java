/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins;

import hudson.Plugin;
import hudson.plugins.analysis.views.DetailFactory;

import com.parasoft.xtest.logging.api.ParasoftLogger;
import com.parasoft.xtest.logging.java.JavaLoggingHandlerFactory;
import com.parasoft.xtest.reports.jenkins.internal.services.JenkinsServicesProvider;

/** 
 * Parasoft plugin for Jenkins
 */
public class ParasoftPlugin
    extends Plugin
{

    @Override
    public void start()
    {
        ParasoftDetailBuilder detailBuilder = new ParasoftDetailBuilder();
        DetailFactory.addDetailBuilder(ParasoftResultAction.class, detailBuilder);
        
        ParasoftLogger.setCurrentFactory(new JavaLoggingHandlerFactory());
        JenkinsServicesProvider.init();
    }
}
