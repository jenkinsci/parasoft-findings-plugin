/*
 * $Id$
 * 
 * (C) Copyright Parasoft Corporation 2011. All rights reserved. THIS IS
 * UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft The copyright notice above
 * does not evidence any actual or intended publication of such source code.
 */


package com.parasoft.xtest.reports.jenkins.internal;

import java.io.File;
import java.util.Properties;

import com.parasoft.xtest.common.api.progress.EmptyProgressMonitor;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.logging.api.ParasoftLogger;
import com.parasoft.xtest.logging.java.JavaLoggingHandlerFactory;
import com.parasoft.xtest.reports.jenkins.internal.services.JenkinsServicesProvider;
import com.parasoft.xtest.reports.preferences.FileImportPreferences;
import com.parasoft.xtest.results.api.importer.IImportPreferences;
import com.parasoft.xtest.results.api.importer.IImportedData;
import com.parasoft.xtest.results.api.importer.IViolationImportResult;
import com.parasoft.xtest.results.api.importer.IViolationImporterService;
import com.parasoft.xtest.services.api.IServicesProvider;
import com.parasoft.xtest.services.api.ServiceUtil;

/**
 * Loads results from report xml file.
 */
public class JenkinsResultsImporter
{

    private final Properties _properties;

    /**
     * @param properties non-default settings to use to load results.
     */
    public JenkinsResultsImporter(Properties properties)
    {
        _properties = properties;
    }

    /**
     * Imports results from given xml file.
     * 
     * @param file source xml file
     * @return import result or null if import cannot be performed.
     */
    public IImportedData performImport(File file)
    {
        JenkinsServicesProvider.init();
        ParasoftLogger.setCurrentFactory(new JavaLoggingHandlerFactory());
        Logger.getLogger().info("Service initialization"); //$NON-NLS-1$
        
        IViolationImporterService service = ServiceUtil.getService(IViolationImporterService.class, new RawServiceContext(_properties));
        if (service == null) {
            Logger.getLogger().warn("Report importer service is null"); //$NON-NLS-1$
            IServicesProvider servicesProvider = ServiceUtil.getServicesProvider();
            if (servicesProvider == null) {
                Logger.getLogger().warn("Services provider not registered"); //$NON-NLS-1$
            }
            return null;
        }
        if (_properties.isEmpty()) {
            Logger.getLogger().warn("Empty properties"); //$NON-NLS-1$
        }
        Logger.getLogger().info("Properties used in importResults " + _properties); //$NON-NLS-1$

        IImportPreferences prefs = new FileImportPreferences(file);
        IViolationImportResult importResult = service.importViolations(prefs, EmptyProgressMonitor.getInstance());
        return (IImportedData)importResult;
    }

}
