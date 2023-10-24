package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProviderFactory;
import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.Node;
import hudson.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class ModifiedLinesCoverageTableModelTest extends AbstractCoverageTest {
    ModifiedLinesCoverageTableModel modifiedLinesCoverageTableModel;
    Node coberturaResult = readCoberturaResult("cobertura-codingstyle.xml");

    @BeforeEach
    public void setup() {
        CoverageTableModel.RowRenderer rowRenderer = new CoverageTableModel.InlineRowRenderer();
        ColorProvider defaultColorProvider = ColorProviderFactory.createDefaultColorProvider();
        Node changeNode = readCoberturaResult("cobertura-codingstyle-no-data.xml");

        modifiedLinesCoverageTableModel = new ModifiedLinesCoverageTableModel("coverage-table-inline", coberturaResult,
                changeNode, rowRenderer, defaultColorProvider);
        assertThat(modifiedLinesCoverageTableModel).isNotNull();
    }

    @Test
    public void testCreateRow() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Locale browserLocale = Functions.getCurrentLocale();
        FileNode fileNode = coberturaResult.getAllFileNodes().get(0);
        Object row = modifiedLinesCoverageTableModel.createRow(fileNode, browserLocale);

        Class modifiedLinesCoverageRow = row.getClass();
        Method getLoc = modifiedLinesCoverageRow.getDeclaredMethod("getLoc");
        Object loc = getLoc.invoke(row);
        assertThat(row).isNotNull();
        assertThat(loc).isEqualTo(0);
    }
}
