package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import hudson.model.View;
import hudson.views.ListViewColumn;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerTest;
import org.junit.jupiter.api.Test;

import static com.parasoft.findings.jenkins.coverage.api.metrics.Assertions.assertThat;

/**
 * Tests support for column and job configurations via the Job DSL Plugin.
 *
 * @author Ullrich Hafner
 */
class JobDslITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Creates a freestyle job from a YAML file and verifies that issue recorder finds warnings.
     */
    @Test
    void shouldCreateColumnFromYamlConfiguration() {
        configureJenkins("column-metric-dsl.yaml");

        View view = getJenkins().getInstance().getView("dsl-view");

        assertThat(view).isNotNull();

        assertThat(view.getColumns())
                .extracting(ListViewColumn::getColumnCaption)
                .contains(new CoverageMetricColumn().getColumnCaption());
    }

    /**
     * Helper method to get jenkins configuration file.
     *
     * @param fileName
     *         file with configuration.
     */
    private void configureJenkins(final String fileName) {
        try {
            ConfigurationAsCode.get().configure(getResourceAsFile(fileName).toUri().toString());
        }
        catch (ConfiguratorException e) {
            throw new AssertionError(e);
        }
    }
}
