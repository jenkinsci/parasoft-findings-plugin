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
