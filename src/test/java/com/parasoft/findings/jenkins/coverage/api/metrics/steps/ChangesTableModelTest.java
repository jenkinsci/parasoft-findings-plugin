package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProviderFactory;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.ChangesTableModel.ChangesRow;
import com.parasoft.findings.jenkins.coverage.api.metrics.steps.CoverageTableModel.RowRenderer;
import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.Node;
import hudson.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangesTableModelTest extends AbstractCoverageTest {
    ChangesTableModel changesTableModel;
    Node coberturaResult = readCoberturaResult("cobertura-codingstyle.xml");
    RowRenderer rowRenderer = new CoverageTableModel.InlineRowRenderer();
    ColorProvider defaultColorProvider = ColorProviderFactory.createDefaultColorProvider();
    FileNode fileNode = coberturaResult.getAllFileNodes().get(0);

    @BeforeEach
    public void setup() {
        Node changeNode = readCoberturaResult("cobertura-codingstyle-no-data.xml");
        changesTableModel = new ChangesTableModel("id", coberturaResult, changeNode, rowRenderer, defaultColorProvider) {
            CoverageRow createRow(FileNode file, Locale browserLocale) {
                return new CoverageRow(fileNode, browserLocale, rowRenderer, defaultColorProvider);
            }
        };
        assertThat(changesTableModel).isNotNull();
    }

    @Test
    public void testGetRows() {
        List<Object> rows = changesTableModel.getRows();
        assertThat(rows.size()).isEqualTo(0);
    }

    @Test
    public void testGetOriginalNode() {
        FileNode originalNode = changesTableModel.getOriginalNode(fileNode);
        assertThat(originalNode).isEqualTo(fileNode);
    }

    @Test
    public void testChangesRow() {
        Locale browserLocale = Functions.getCurrentLocale();
        FileNode changedFileNode = coberturaResult.getAllFileNodes().get(1);

        ChangesRow changesRow = new ChangesRow(fileNode, changedFileNode, browserLocale,
                rowRenderer, defaultColorProvider);

        FileNode originalFile = changesRow.getOriginalFile();
        assertThat(originalFile).isEqualTo(fileNode);
    }
}
