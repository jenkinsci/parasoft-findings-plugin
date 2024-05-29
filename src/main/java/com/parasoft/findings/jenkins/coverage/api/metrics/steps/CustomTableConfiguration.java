/*
 * Copyright 2023 Parasoft Corporation
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

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.PluginWrapper;
import io.jenkins.plugins.datatables.TableConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomTableConfiguration extends TableConfiguration {

    private final Map<String, Object> customConfiguration = new HashMap<>();

    public void loadConfiguration() {
        String tableConfiguration = super.getConfiguration();
        try {
            customConfiguration.putAll(new ObjectMapper().readValue(tableConfiguration, new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("Can't read table configuration '%s' JSON object", tableConfiguration), e);
        }
    }

    public void language(String i18nFileBasename) {
        if (i18nFileBasename != null) {
            Jenkins jenkins = Jenkins.get();
            String jenkinsRootUrlFromRequest = jenkins.getRootUrlFromRequest();
            PluginWrapper pluginWrapper = jenkins.getPluginManager().getPlugin("parasoft-findings");
            if (pluginWrapper != null) {
                String pluginDir = pluginWrapper.baseResourceURL.getFile();
                File wantedLocalizationFile = new File(pluginDir, "/i18n/datatables.net/" + i18nFileBasename + ".json");
                if(!wantedLocalizationFile.exists()) {
                    // Use English as default if the wanted language file does not exist
                    i18nFileBasename = "datatables";
                }

                String jenkinsResourcePath = StringUtils.removeStart(Jenkins.RESOURCE_PATH, "/");
                String finalLocalizationRequestUrl = jenkinsRootUrlFromRequest + jenkinsResourcePath + "/plugin/parasoft-findings/i18n/datatables.net/" + i18nFileBasename + ".json";
                customConfiguration.put("language", new Language(finalLocalizationRequestUrl));
            }
        }
    }

    /**
     * Get the configuration as JSON.
     *
     * @return a JSON Object with the configuration
     */
    public String getConfiguration() {
        try {
            return new ObjectMapper().writeValueAsString(customConfiguration);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    String.format("Can't convert table configuration '%s' to JSON object", customConfiguration), exception);
        }
    }

    public static class Language {
        private final String url;

        public Language(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
