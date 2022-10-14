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

package com.parasoft.xtest.reports.jenkins.internal;

import com.parasoft.xtest.common.api.ISystemService;
import com.parasoft.xtest.common.api.progress.EmptyProgressMonitor;
import com.parasoft.xtest.common.crypto.CryptUtil;
import com.parasoft.xtest.common.locations.IImportLocationMatcher;
import com.parasoft.xtest.common.services.IStorableServiceContext;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.logging.api.ParasoftLogger;
import com.parasoft.xtest.logging.java.JavaLoggingHandlerFactory;
import com.parasoft.xtest.reports.jenkins.internal.services.JenkinsServicesProvider;
import com.parasoft.xtest.reports.preferences.FileImportPreferences;
import com.parasoft.xtest.results.api.importer.*;
import com.parasoft.xtest.results.xapi.IMatchingViolationImporter;
import com.parasoft.xtest.services.api.IServicesProvider;
import com.parasoft.xtest.services.api.ServiceUtil;

import java.io.File;
import java.util.Properties;

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
        
        IStorableServiceContext rawContext = new RawServiceContext(_properties);
        IViolationImporterService service = ServiceUtil.getService(IViolationImporterService.class, rawContext);
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
        logProperties(rawContext);

        IImportPreferences prefs = new FileImportPreferences(file);
        IViolationImportResult importResult = null;
        if (service instanceof IMatchingViolationImporter) {
            IImportLocationMatcher matcher = new JenkinsLocationMatcher();
            importResult = ((IMatchingViolationImporter)service).importViolations(prefs, matcher, EmptyProgressMonitor.getInstance());
        } else {
            importResult = service.importViolations(prefs, EmptyProgressMonitor.getInstance());
        }
        return (IImportedData)importResult;
    }

    private void logProperties(IStorableServiceContext rawContext)
    {
        ISystemService sensitiveKeysService = ServiceUtil.getService(ISystemService.class, rawContext);
        if (sensitiveKeysService == null) {
            Logger.getLogger().warn("Sensitive Keys Service service is null"); //$NON-NLS-1$
            return;
        }
        Properties result = new Properties();
        for (Object sKey : _properties.keySet()) {
            String sKeyString = String.valueOf(sKey);
            boolean canPrintValue = !sensitiveKeysService.isSensitiveSetting(sKeyString);
            String sValue = _properties.getProperty(sKeyString);
            String sPrintedValue = canPrintValue ? sValue : CryptUtil.anonymize(sValue);
            result.setProperty(sKeyString, sPrintedValue);
        }
        Logger.getLogger().info("Properties used in importResults " + result); //$NON-NLS-1$
    }
}
