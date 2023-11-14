package com.parasoft.findings.jenkins.coverage.api.metrics.model;

import com.parasoft.findings.jenkins.coverage.model.*;
import hudson.util.ListBoxModel;
import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@DefaultLocale("en")
public class ElementFormatterTest {
    private static final int COVERED_LINES = 28;
    private static final int MISSED_LINES = 8;
    private static final int TOTAL_LINES = 36;
    private ElementFormatter formatter;
    private Coverage coverage;
    private IntegerValue integerValue;
    private Fraction fraction;
    private FractionValue fractionValue;

    @BeforeEach
    void generateFormatter() {
        formatter = new ElementFormatter();
        coverage = new Coverage.CoverageBuilder().setMetric(Metric.LINE).setCovered(COVERED_LINES)
                .setMissed(MISSED_LINES)
                .build();
        integerValue = new LinesOfCode(TOTAL_LINES);
        fraction = Fraction.getFraction(COVERED_LINES, TOTAL_LINES);
        fractionValue = new FractionValue(Metric.LINE, fraction);
    }

    @Test
    void testGetDisplayName() {
        var displayName = formatter.getDisplayName(Metric.CONTAINER);
        assertThat(displayName).isEqualTo("Container Coverage");

        displayName = formatter.getDisplayName(Metric.MODULE);
        assertThat(displayName).isEqualTo("Module Coverage");

        displayName = formatter.getDisplayName(Metric.PACKAGE);
        assertThat(displayName).isEqualTo("Package Coverage");

        displayName = formatter.getDisplayName(Metric.FILE);
        assertThat(displayName).isEqualTo("File Coverage");

        displayName = formatter.getDisplayName(Metric.CLASS);
        assertThat(displayName).isEqualTo("Class Coverage");

        displayName = formatter.getDisplayName(Metric.METHOD);
        assertThat(displayName).isEqualTo("Method Coverage");

        displayName = formatter.getDisplayName(Metric.LINE);
        assertThat(displayName).isEqualTo("Coverage");

        displayName = formatter.getDisplayName(Metric.BRANCH);
        assertThat(displayName).isEqualTo("Branch Coverage");

        displayName = formatter.getDisplayName(Metric.INSTRUCTION);
        assertThat(displayName).isEqualTo("Instruction Coverage");

        displayName = formatter.getDisplayName(Metric.LOC);
        assertThat(displayName).isEqualTo("Lines covered");

        try {
            formatter.getDisplayName(Metric.COMPLEXITY_DENSITY);
        } catch (NoSuchElementException e) {
            assertThat(e).hasMessageContaining("No display name found for metric COMPLEXITY_DENSITY");
        }
    }

    @Test
    void testGetLabel() {
        var label = formatter.getLabel(Metric.CONTAINER, null);
        assertThat(label).isEqualTo("Container");

        label = formatter.getLabel(Metric.MODULE, null);
        assertThat(label).isEqualTo("Module");

        label = formatter.getLabel(Metric.PACKAGE, null);
        assertThat(label).isEqualTo("Folder");

        label = formatter.getLabel(Metric.PACKAGE, "Jtest");
        assertThat(label).isEqualTo("Package");

        label = formatter.getLabel(Metric.PACKAGE, "dotTEST");
        assertThat(label).isEqualTo("Namespace");

        label = formatter.getLabel(Metric.FILE, null);
        assertThat(label).isEqualTo("File");

        label = formatter.getLabel(Metric.CLASS, null);
        assertThat(label).isEqualTo("Class");

        label = formatter.getLabel(Metric.METHOD, null);
        assertThat(label).isEqualTo("Method");

        label = formatter.getLabel(Metric.LINE, null);
        assertThat(label).isEqualTo("Line");

        label = formatter.getLabel(Metric.BRANCH, null);
        assertThat(label).isEqualTo("Branch");

        label = formatter.getLabel(Metric.INSTRUCTION, null);
        assertThat(label).isEqualTo("Instruction");

        label = formatter.getLabel(Metric.LOC, null);
        assertThat(label).isEqualTo("LOC");

        try {
            formatter.getLabel(Metric.COMPLEXITY_DENSITY, null);
        } catch (NoSuchElementException e) {
            assertThat(e).hasMessageContaining("No label found for metric COMPLEXITY_DENSITY");
        }
    }

    @Test
    void testGetTypeItems() {
        ListBoxModel listBoxModel = formatter.getTypeItems();
        assertThat(listBoxModel.get(0).toString()).isEqualTo("Overall project=PROJECT");
        assertThat(listBoxModel.get(1).toString()).isEqualTo("Modified code lines=MODIFIED_LINES");
    }

    @Test
    void testGetCriticalityItems() {
        ListBoxModel listBoxModel = formatter.getCriticalityItems();
        assertThat(listBoxModel.get(0).toString()).isEqualTo("Set the build status to unstable if the quality gate fails=UNSTABLE");
        assertThat(listBoxModel.get(1).toString()).isEqualTo("Set the build status to failed if the quality gate fails=FAILURE");
    }

    @Test
    void testFormatDetails() {
        String result = formatter.formatValue(coverage);
        assertThat(result).isEqualTo("77.78% (28/36)");

        coverage = new Coverage.CoverageBuilder().setMetric(Metric.LINE).setCovered(0)
                .setMissed(0)
                .build();
        result = formatter.formatValue(coverage);
        assertThat(result).isEqualTo("-");

        result = formatter.formatValue(integerValue);
        assertThat(result).isEqualTo("36");

        result = formatter.formatValue(fractionValue);
        assertThat(result).isEqualTo("0.78%");

    }

    @Test
    void testFormat() {
        String result = formatter.format(coverage);
        assertThat(result).isEqualTo("77.78%");

        result = formatter.format(integerValue);
        assertThat(result).isEqualTo("28/36");

        result = formatter.format(fractionValue);
        assertThat(result).isEqualTo("LINE: 28/36");
    }

    @Test
    void testGetDisplayColors() {
        var result = formatter.getDisplayColors(Baseline.PROJECT,coverage);
        assertThat(result.toString()).isEqualTo("java.awt.Color[r=0,g=0,b=0] - java.awt.Color[r=254,g=199,b=24]");

        result = formatter.getDisplayColors(Baseline.PROJECT, integerValue);
        assertThat(result.toString()).isEqualTo("java.awt.Color[r=0,g=0,b=0] - java.awt.Color[r=255,g=255,b=255]");

        result = formatter.getDisplayColors(Baseline.PROJECT, fractionValue);
        assertThat(result.toString()).isEqualTo("java.awt.Color[r=255,g=255,b=255] - java.awt.Color[r=230,g=1,b=32]");
    }

    @Test
    void testFormatValueWithMetric() {
        var result = formatter.formatValueWithMetric(coverage);
        assertThat(result).isEqualTo("Coverage: 77.78%");
    }

    @Test
    void testFormatDetailedValueWithMetric() {
        var result = formatter.formatDetailedValueWithMetric(coverage);
        assertThat(result).isEqualTo("Coverage: 77.78% (28/36)");
    }

    @Test
    void testGetBackgroundColorFillPercentage() {
        var result = formatter.getBackgroundColorFillPercentage(coverage);
        assertThat(result).isEqualTo("77.78%");

        result = formatter.getBackgroundColorFillPercentage(fractionValue);
        assertThat(result).isEqualTo("100%");
    }

    @Test
    void testFormatAdditionalInformation() {
        var result = formatter.formatAdditionalInformation(coverage);
        assertThat(result).isEqualTo("Covered: 28 - Missed: 8");

        coverage = new Coverage.CoverageBuilder().setMetric(Metric.LINE).setCovered(0)
                .setMissed(0)
                .build();
        result = formatter.formatAdditionalInformation(coverage);
        assertThat(result).isEqualTo("");

        result = formatter.formatAdditionalInformation(fractionValue);
        assertThat(result).isEqualTo("");
    }
}
