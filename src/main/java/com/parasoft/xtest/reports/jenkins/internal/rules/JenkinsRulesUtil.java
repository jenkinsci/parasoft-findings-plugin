/*
 * $Id$
 * 
 * (C) Copyright Parasoft Corporation 2011. All rights reserved. THIS IS
 * UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft The copyright notice above
 * does not evidence any actual or intended publication of such source code.
 */


package com.parasoft.xtest.reports.jenkins.internal.rules;

import java.util.Properties;

import com.parasoft.xtest.common.api.progress.EmptyProgressMonitor;
import com.parasoft.xtest.common.api.progress.IProgressMonitor;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.configuration.api.rules.IRuleDescriptionUpdateService;
import com.parasoft.xtest.services.api.IParasoftServiceContext;
import com.parasoft.xtest.services.api.ServiceUtil;

/**
 * Utility class for operations related to rules.
 */
public class JenkinsRulesUtil
{
    /**
     * Refreshes rules information if rule providers have non-static rule descriptions and are capable of update during runtime.
     * This requires given settings to contain complete information required to perform such update. 
     * 
     * @param settings settings to use to refresh rules
     */
    public static void refreshRuleDescriptions(Properties settings)
    {
        IParasoftServiceContext context = new RawServiceContext(settings);
        
        IProgressMonitor monitor = EmptyProgressMonitor.getInstance();
        IRuleDescriptionUpdateService ruleDescriptionUpdateService = ServiceUtil.getService(IRuleDescriptionUpdateService.class);
        if (ruleDescriptionUpdateService != null) {
            ruleDescriptionUpdateService.refreshSharedDescriptions(context, monitor.subTask(1));
        }
    }
}
