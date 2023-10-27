package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProviderFactory;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTableModel.CoverageRow;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTableModel.LinkedRowRenderer;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTableModel.RowRenderer;
import com.parasoft.findings.jenkins.coverage.model.*;
import hudson.Functions;
import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageViewModel.MODIFIED_LINES_COVERAGE_TABLE_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class CoverageTableModelTest extends AbstractCoverageTest {
    CoverageTableModel coverageTableModel;
    Node coberturaResult = readCoberturaResult("cobertura-codingstyle.xml");
    RowRenderer rowRenderer = new CoverageTableModel.InlineRowRenderer();
    ColorProvider defaultColorProvider = ColorProviderFactory.createDefaultColorProvider();

    @BeforeEach
    public void setup() {
        coverageTableModel = new CoverageTableModel("coverage-table-inline", coberturaResult,
                rowRenderer, defaultColorProvider);
        assertThat(coverageTableModel).isNotNull();
    }

    @Test
    public void testGetRenderer() {
        RowRenderer render = coverageTableModel.getRenderer();
        assertThat(render).isEqualTo(rowRenderer);
    }

    @Test
    public void testGetTableConfiguration() {
        TableConfiguration tableConfiguration = coverageTableModel.getTableConfiguration();
        assertThat(tableConfiguration).isNotNull();
    }

    @Test
    public void testGetColumns() {
        List<TableColumn> tableColumns = coverageTableModel.getColumns();
        assertThat(tableColumns.size()).isEqualTo(5);
        assertThat(tableColumns.get(0).getHeaderLabel()).isEqualTo("Hash");
        assertThat(tableColumns.get(1).getHeaderLabel()).isEqualTo("File");
        assertThat(tableColumns.get(2).getHeaderLabel()).isEqualTo("Folder");
        assertThat(tableColumns.get(3).getHeaderLabel()).isEqualTo("Line");
        assertThat(tableColumns.get(4).getHeaderLabel()).isEqualTo("LOC");
    }

    @Test
    public void testGetRows() {
        List<Object> rows = coverageTableModel.getRows();
        assertThat(rows.size()).isEqualTo(5);
    }

    @Test
    public void testGetRoot() {
        Node root = coverageTableModel.getRoot();
        assertThat(root).isEqualTo(coberturaResult);
    }

    @Test
    public void testGetColorProvider() {
        ColorProvider colorProvider = coverageTableModel.getColorProvider();
        assertThat(colorProvider).isEqualTo(defaultColorProvider);
    }

    @Test
    public void testCoverageRow() {
        Locale browserLocale = Functions.getCurrentLocale();
        FileNode fileNode = coberturaResult.getAllFileNodes().get(0);
        CoverageRow coverageRow = new CoverageRow(fileNode, browserLocale, rowRenderer, defaultColorProvider);

        //Test getFileHash()
        String fileHash = coverageRow.getFileHash();
        assertThat(fileHash).isEqualTo("-852076116");

        // Test getFileName()
        DetailedCell<?> fileName = coverageRow.getFileName();
        assertThat(fileName.getDisplay()).isEqualTo("Calculator.java");

        // Test getLineCoverage() and can create the column with coverage
        DetailedCell<?> lineCoverage = coverageRow.getLineCoverage();
        assertThat(lineCoverage.getDisplay()).isEqualTo("<div class=\"coverage-cell-outer float-end\"><div class=\"coverage-jenkins-cell-inner\" style=\"background-image: linear-gradient(90deg, #1EA64B50 100.000000%, transparent 100.000000%);\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Covered: 12 - Missed: 0\">100.00%</div></div>");
        assertThat(lineCoverage.getSort()).isEqualTo(100.0);

        // Test create the column without coverage
        DetailedCell<?> result = coverageRow.createColoredCoverageColumn(coverageRow.getCoverageOfNode(Metric.COMPLEXITY));
        assertThat(result.getDisplay()).isEqualTo("N/A");
        assertThat(result.getSort()).isEqualTo(-1000);

        // Test getPackageName()
        String packageName = coverageRow.getPackageName();
        assertThat(packageName).isEqualTo("<default>");

        // Test getLoc()
        int loc = coverageRow.getLoc();
        assertThat(loc).isEqualTo(12);

        // Test getComplexity()
        int complexity = coverageRow.getComplexity();
        assertThat(complexity).isEqualTo(0);

        // Test getFile()
        FileNode file = coverageRow.getFile();
        assertThat(file).isEqualTo(fileNode);
    }

    @Test
    public void testLinkedRowRenderer() {
        RowRenderer linkedRowRenderer = new LinkedRowRenderer(new File("src/test/resources/com/parasoft/findings/jenkins/coverage/api/metrics/steps"), "", MODIFIED_LINES_COVERAGE_TABLE_ID);
        String fileName = linkedRowRenderer.renderFileName("Calculator.java", "src_main_java_Calculator.java");
        assertThat(fileName).isEqualTo("<a href=\"-839020164?tableId=modified-lines-coverage-table\">Calculator.java</a>");
    }

    @Test
    public void testLinkedRowRenderer_noSourceCodeFile() {
        RowRenderer linkedRowRenderer = new LinkedRowRenderer(new File("src/test/resources/com/parasoft/findings/jenkins/coverage/api/metrics/steps"), "", MODIFIED_LINES_COVERAGE_TABLE_ID);
        String fileName = linkedRowRenderer.renderFileName("NoSourceCodeFile.java", "src_main_java_NoSourceCodeFile.java");
        assertThat(fileName).isEqualTo("NoSourceCodeFile.java");
    }
}