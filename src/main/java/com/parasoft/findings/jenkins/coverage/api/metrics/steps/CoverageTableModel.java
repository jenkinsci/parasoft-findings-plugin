/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.parasoft.findings.jenkins.coverage.model.Coverage;
import com.parasoft.findings.jenkins.coverage.model.CyclomaticComplexity;
import com.parasoft.findings.jenkins.coverage.model.FileNode;
import com.parasoft.findings.jenkins.coverage.model.LinesOfCode;
import com.parasoft.findings.jenkins.coverage.model.Metric;
import com.parasoft.findings.jenkins.coverage.model.Node;

import hudson.Functions;

import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.ColorProvider.DisplayColors;
import com.parasoft.findings.jenkins.coverage.api.metrics.color.CoverageLevel;
import com.parasoft.findings.jenkins.coverage.api.metrics.model.ElementFormatter;
import com.parasoft.findings.jenkins.coverage.api.metrics.source.SourceCodeFacade;
import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.TableColumn.ColumnType;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableConfiguration.SelectStyle;
import io.jenkins.plugins.datatables.TableModel;
import org.jvnet.localizer.LocaleProvider;

import static j2html.TagCreator.*;

/**
 * UI table model for the coverage details table.
 */
class CoverageTableModel extends TableModel {
    private static final int NO_COVERAGE_SORT = -1_000;
    private static final SourceCodeFacade SOURCE_CODE_FACADE = new SourceCodeFacade();

    /**
     * The alpha value for colors to be used to highlight the coverage within the table view.
     */
    private static final int TABLE_COVERAGE_COLOR_ALPHA = 80;

    static final DetailedCell<Integer> NO_COVERAGE
            = new DetailedCell<>(Messages.Coverage_Not_Available(), NO_COVERAGE_SORT);

    private final ColorProvider colorProvider;
    private final Node root;
    private final RowRenderer renderer;
    private final String id;

    CoverageTableModel(final String id, final Node root, final RowRenderer renderer, final ColorProvider colors) {
        super();

        this.id = id;
        this.root = root;
        this.renderer = renderer;
        colorProvider = colors;
    }

    RowRenderer getRenderer() {
        return renderer;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TableConfiguration getTableConfiguration() {
        CustomTableConfiguration customTableConfiguration = new CustomTableConfiguration();
        customTableConfiguration.responsive();
        if (getId().contains("inline")) {
            customTableConfiguration.select(SelectStyle.SINGLE);
        }

        customTableConfiguration.loadConfiguration();
        Locale languageCode = LocaleProvider.getLocale();
        String i18nFileBasename = null;
        if (languageCode.equals(Locale.SIMPLIFIED_CHINESE)) {
            i18nFileBasename = "zh";
        }
        customTableConfiguration.language(i18nFileBasename);
        renderer.configureTable(customTableConfiguration);
        return customTableConfiguration;
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();

        TableColumn fileHash = new ColumnBuilder().withHeaderLabel("Hash")
                .withDataPropertyKey("fileHash")
                .withHeaderClass(ColumnCss.HIDDEN)
                .build();
        columns.add(fileHash);
        TableColumn fileName = new ColumnBuilder().withHeaderLabel(Messages.Column_File())
                .withDataPropertyKey("fileName")
                .withDetailedCell()
                .withResponsivePriority(1)
                .build();
        columns.add(fileName);
        TableColumn packageName = new ColumnBuilder().withHeaderLabel(Messages.Column_Package())
                .withDataPropertyKey("packageName")
                .withResponsivePriority(50_000)
                .build();
        columns.add(packageName);

        configureValueColumn("lineCoverage", Metric.LINE, Messages.Column_LineCoverage(), columns);
        TableColumn loc = new ColumnBuilder().withHeaderLabel(Messages.Column_LinesOfCode())
                .withDataPropertyKey("loc")
                .withResponsivePriority(200)
                .withType(ColumnType.NUMBER)
                .build();
        columns.add(loc);
        return columns;
    }

    private void configureValueColumn(final String key, final Metric metric, final String headerLabel, final List<TableColumn> columns) {
        if (root.containsMetric(metric)) {
            TableColumn lineCoverage = new ColumnBuilder().withHeaderLabel(headerLabel)
                    .withDataPropertyKey(key)
                    .withDetailedCell()
                    .withType(ColumnType.NUMBER)
                    .withResponsivePriority(1)
                    .build();
            columns.add(lineCoverage);
        }
    }

    @Override
    public List<Object> getRows() {
        Locale browserLocale = Functions.getCurrentLocale();
        return root.getAllFileNodes().stream()
                .map(file -> new CoverageRow(file, browserLocale, renderer, colorProvider))
                .collect(Collectors.toList());
    }

    protected Node getRoot() {
        return root;
    }

    protected ColorProvider getColorProvider() {
        return colorProvider;
    }

    /**
     * UI row model for the coverage details table.
     */
    static class CoverageRow {
        private static final String COVERAGE_COLUMN_OUTER = "coverage-cell-outer float-end";
        private static final String COVERAGE_COLUMN_INNER = "coverage-jenkins-cell-inner";
        private static final ElementFormatter FORMATTER = new ElementFormatter();
        private static final LinesOfCode ZERO_LOC = new LinesOfCode(0);
        private static final CyclomaticComplexity ZERO_COMPLEXITY = new CyclomaticComplexity(0);

        private final FileNode file;
        private final Locale browserLocale;
        private final RowRenderer renderer;
        private final ColorProvider colorProvider;

        CoverageRow(final FileNode file, final Locale browserLocale, final RowRenderer renderer,
                final ColorProvider colors) {
            this.file = file;
            this.browserLocale = browserLocale;
            this.renderer = renderer;
            colorProvider = colors;
        }

        public String getFileHash() {
            return String.valueOf(file.getRelativePath().hashCode());
        }

        public DetailedCell<?> getFileName() {
            return new DetailedCell<>(renderer.renderFileName(file.getName(), file.getRelativePath()), file.getName());
        }

        public String getPackageName() {
            return file.getParentName();
        }

        public DetailedCell<?> getLineCoverage() {
            return createColoredCoverageColumn(getCoverageOfNode(Metric.LINE));
        }

        Coverage getCoverageOfNode(final Metric metric) {
            return file.getTypedValue(metric, Coverage.nullObject(metric));
        }

        public int getLoc() {
            return file.getTypedValue(Metric.LOC, ZERO_LOC).getValue();
        }

        public int getComplexity() {
            return file.getTypedValue(Metric.COMPLEXITY, ZERO_COMPLEXITY).getValue();
        }

        /**
         * Creates a table cell which colorizes the shown coverage dependent on the coverage percentage.
         *
         * @param coverage
         *         the coverage of the element
         *
         * @return the new {@link DetailedCell}
         */
        protected DetailedCell<?> createColoredCoverageColumn(final Coverage coverage) {
            if (coverage.isSet()) {
                double percentage = coverage.getCoveredPercentage().toDouble();
                DisplayColors colors = CoverageLevel.getDisplayColorsOfCoverageLevel(percentage, colorProvider);
                String cell = div()
                        .withClasses(COVERAGE_COLUMN_OUTER).with(
                                div().withClasses(COVERAGE_COLUMN_INNER)
                                        .withStyle(String.format(
                                                "background-image: linear-gradient(90deg, %s %f%%, transparent %f%%);",
                                                colors.getFillColorAsRGBAHex(TABLE_COVERAGE_COLOR_ALPHA),
                                                percentage, percentage))
                                        .attr("data-bs-toggle", "tooltip")
                                        .attr("data-bs-placement", "top")
                                        .withTitle(FORMATTER.formatAdditionalInformation(coverage))
                                        .withText(FORMATTER.formatPercentage(coverage, browserLocale)))
                        .render();
                return new DetailedCell<>(cell, percentage);
            }
            return NO_COVERAGE;
        }

        protected FileNode getFile() {
            return file;
        }
    }

    /**
     * Renders filenames with links. Selection will be handled by opening a new page using the provided link.
     */
    static class LinkedRowRenderer implements RowRenderer {
        private final File buildFolder;
        private final String resultsId;

        LinkedRowRenderer(final File buildFolder, final String resultsId) {
            this.buildFolder = buildFolder;
            this.resultsId = resultsId;
        }

        @Override
        public void configureTable(final TableConfiguration tableConfiguration) {
            // nothing required
        }

        @Override
        public String renderFileName(final String fileName, final String path) {
            if (SOURCE_CODE_FACADE.canRead(buildFolder, resultsId, path)) {
                return a().withHref(String.valueOf(path.hashCode())).withText(fileName).render();
            }
            return fileName;
        }
    }

    /**
     * Renders filenames without links. Selection will be handled using the table select events.
     */
    static class InlineRowRenderer implements RowRenderer {
        @Override
        public void configureTable(final TableConfiguration tableConfiguration) {
            tableConfiguration.select(SelectStyle.SINGLE);
        }

        @Override
        public String renderFileName(final String fileName, final String path) {
            return fileName;
        }
    }

    /**
     * Renders filenames in table cells.
     */
    interface RowRenderer {
        void configureTable(TableConfiguration tableConfiguration);

        String renderFileName(String fileName, String path);
    }
}
