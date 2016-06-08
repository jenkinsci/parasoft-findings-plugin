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

package com.parasoft.xtest.reports.jenkins.internal.services;

import java.util.Properties;

import com.parasoft.xtest.common.api.parallel.IParallelRunner;
import com.parasoft.xtest.common.application.IApplication;
import com.parasoft.xtest.common.application.OSGiApplication;
import com.parasoft.xtest.common.dtp.*;
import com.parasoft.xtest.common.parallel.ParallelExecutor;
import com.parasoft.xtest.common.preferences.ConfigurationPreferencesFactory;
import com.parasoft.xtest.common.preferences.IConfigurationPreferences;
import com.parasoft.xtest.common.preferences.IPreferences;
import com.parasoft.xtest.common.preferences.PreferencesServiceUtil;
import com.parasoft.xtest.common.services.DefaultServicesProvider;
import com.parasoft.xtest.configuration.api.IPreferencesService;
import com.parasoft.xtest.configuration.api.rules.IRuleDescriptionUpdateService;
import com.parasoft.xtest.configuration.rules.RuleDescriptionUpdateService;
import com.parasoft.xtest.reports.internal.importers.ViolationImporterServiceFactory;
import com.parasoft.xtest.results.api.IResultPostProcessorService;
import com.parasoft.xtest.results.api.importer.IViolationImporterService;
import com.parasoft.xtest.results.factory.IResultFactory;
import com.parasoft.xtest.results.internal.ResultsInitManager;
import com.parasoft.xtest.results.internal.factory.*;
import com.parasoft.xtest.results.locations.ResultLocationProcessor;
import com.parasoft.xtest.results.rules.RulesProcessor;
import com.parasoft.xtest.results.sourcecontrol.SourceControlProcessor;
import com.parasoft.xtest.results.suppressions.SuppressionsProcessor;
import com.parasoft.xtest.results.xapi.IResultsInitManager;
import com.parasoft.xtest.results.xapi.xml.IViolationXmlStorage;
import com.parasoft.xtest.results.xml.DefaultCodingStandardsViolationStorage;
import com.parasoft.xtest.services.api.ServiceUtil;
import com.parasoft.xtest.share.api.ISharingRepository;
import com.parasoft.xtest.share.internal.dtp.DTPRepositoriesServiceFactory;


/** 
 * Singleton class to set up local service environment required by the plugin to be fully operational.
 */
public final class JenkinsServicesProvider
    extends DefaultServicesProvider
{
    private static JenkinsServicesProvider INSTANCE;
    
    private JenkinsServicesProvider() { }
     
    /**
     * Initialize this class once before using any of the core functionality.
     */
    public static synchronized void init()
    {
        if (INSTANCE == null) {
            INSTANCE = new JenkinsServicesProvider();
            ServiceUtil.setServicesProvider(INSTANCE);
            INSTANCE.initialize();
        }
    }

    private void initialize()
    {
        registerService(IViolationImporterService.Factory.class, new ViolationImporterServiceFactory());
        registerService(IApplication.class, new OSGiApplication());
        registerService(IParallelRunner.class, new ParallelExecutor(null));
        registerService(IResultFactory.class, new DefaultCodingStandardsResultFactory());
        registerService(IViolationXmlStorage.class, new DefaultCodingStandardsViolationStorage());
        registerService(IViolationXmlStorage.class, new FlowAnalysisResultStorage());
        registerService(IViolationXmlStorage.class, new DupcodeViolationStorage());
        registerService(IResultPostProcessorService.class, new SourceControlProcessor());
        registerService(IResultPostProcessorService.class, new ResultLocationProcessor());
        registerService(IResultFactory.class, new DefaultSetupProblemsResultFactory());
        registerService(IResultFactory.class, new DefaultScopeResultFactory());
        registerService(IResultPostProcessorService.class, new SuppressionsProcessor());
        registerService(IResultPostProcessorService.class, new RulesProcessor());
        registerService(IRuleDescriptionUpdateService.class, new RuleDescriptionUpdateService());
        registerService(ISharingRepository.Factory.class, new DTPRepositoriesServiceFactory());
        registerService(IDtpServiceRegistry.Factory.class, new DtpServiceRegistryFactory());
        registerService(IResultsInitManager.class, new ResultsInitManager());
        registerService(IPreferencesService.class, new DtpAutoconfPreferencesService());
        Properties properties = new Properties();
        properties.setProperty(PreferencesServiceUtil.PREFERENCES_ID_PROPERTY, IDtpPreferences.PREFERENCES_ID);
        registerService(IPreferences.Factory.class, new DtpPreferencesFactory(), properties);
        properties = new Properties();
        properties.setProperty(PreferencesServiceUtil.PREFERENCES_ID_PROPERTY, IConfigurationPreferences.PREFERENCES_ID);
        registerService(IPreferences.Factory.class, new ConfigurationPreferencesFactory(), properties);
    }

}
