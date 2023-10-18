package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.util.JenkinsFacade;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

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
            String url = new JenkinsFacade().getAbsoluteUrl(StringUtils.removeStart(Jenkins.RESOURCE_PATH, "/"),
                    String.format("plugin/parasoft-findings/i18n/datatables.net/%s.json", i18nFileBasename));
            customConfiguration.put("language", new Language(url));
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
