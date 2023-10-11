/* global jQuery3, viewProxy, echartsJenkinsApi, bootstrap5 */

getJenkinsColors = function (colors) {
    // TODO: also handle HSL colors and parse them to hex in order to use dark mode colors
    const colorHexMapping = new Map;
    colors.forEach(function (jenkinsId) {
        const colorHex = getComputedStyle(document.body).getPropertyValue(jenkinsId);
        if (colorHex.match(/^#[a-fA-F0-9]{6}$/) !== null) {
            colorHexMapping.set(jenkinsId, colorHex);
        }
    })
    return colorHexMapping;
};

const CoverageChartGenerator = function ($) {

    function printPercentage(value, minimumFractionDigits = 2) {
        return Number(value / 100.0).toLocaleString(undefined, {style: 'percent', minimumFractionDigits: minimumFractionDigits});
    }

    const openBuild = function (build) {
        viewProxy.getUrlForBuild(build, window.location.href, function (buildUrl) {
            if (buildUrl.responseJSON.startsWith('http')) {
                window.location.assign(buildUrl.responseJSON);
            }
        });
    };
    function createOverview(overview, id) {
        const missedColor = echartsJenkinsApi.resolveJenkinsColor("--red");
        const missedText = echartsJenkinsApi.resolveJenkinsColor("--white");
        const coveredColor = echartsJenkinsApi.resolveJenkinsColor("--green");
        const coveredText = echartsJenkinsApi.resolveJenkinsColor("--white");

        const summaryChartDiv = $('#' + id);
        summaryChartDiv.height(overview.metrics.length * 31 + 150 + 'px');
        const summaryChart = echarts.init(summaryChartDiv[0]);
        summaryChartDiv[0].echart = summaryChart;

        const textColor = echartsJenkinsApi.getTextColor();

        const summaryOption = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                },
                formatter: function (obj) {
                    if (Array.isArray(obj)) {
                        if (obj.length === 2) {
                            return ['<b>' + obj[0].name + '</b>',
                                obj[0].seriesName + ': ' + overview.covered[obj[0].dataIndex],
                                obj[1].seriesName + ': ' + overview.missed[obj[1].dataIndex],
                                printPercentage(overview.coveredPercentages[obj[0].dataIndex])

                            ].join('<br/>');
                        }
                        else if (obj.length === 1) {
                            return '<b>' + obj[0].name + '</b><br/>'
                                + obj[0].seriesName + ': '
                                + (obj[0].seriesName === 'Covered' ?
                                    overview.covered[obj[0].dataIndex]
                                    : overview.missed[obj[0].dataIndex]);
                        }
                    }
                }
            },
            legend: {
                data: ['Covered', 'Missed'],
                x: 'center',
                y: 'top',
                textStyle: {
                    color: textColor
                }
            },
            grid: {
                left: '20',
                right: '10',
                bottom: '5',
                top: '40',
                containLabel: true
            },
            xAxis: {
                type: 'value',
                axisLabel: {
                    formatter: function (value) {
                        return printPercentage(value, 0);
                    },
                    color: textColor
                }
            },
            yAxis: [{
                type: 'category',
                data: overview.metrics,
                axisLine: {
                    show: false
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    color: textColor
                }
            }, {
                type: 'category',
                data: overview.coveredPercentages,
                position: 'right',
                axisLabel: {
                    formatter: function (value, index) {
                        return printPercentage(overview.coveredPercentages[index]);
                    },
                    color: textColor
                },
                axisLine: {
                    show: false
                },
                axisTick: {
                    show: false
                }

            }],
            series: [
                {
                    name: 'Covered',
                    type: 'bar',
                    stack: 'sum',
                    itemStyle: {
                        normal: {
                            color: coveredColor
                        }
                    },
                    label: {
                        show: true,
                        position: 'insideLeft',
                        color: coveredText,
                        fontWeight: 'bold',
                        formatter: function (obj) {
                            return overview.covered[obj.dataIndex];
                        }
                    },
                    data: overview.coveredPercentages
                },
                {
                    name: 'Missed',
                    type: 'bar',
                    stack: 'sum',
                    itemStyle: {
                        normal: {
                            color: missedColor
                        }
                    },
                    label: {
                        show: true,
                        position: 'insideRight',
                        color: missedText,
                        fontWeight: 'bold',
                        formatter: function (obj) {
                            return overview.missed[obj.dataIndex];
                        }
                    },
                    data: overview.missedPercentages
                }
            ]
        };
        summaryChart.setOption(summaryOption);
        summaryChart.resize();
    }

    this.populateDetailsCharts = function (jenkinsColors) {
        /**
         * Activate the tab that has been visited the last time. If there is no such tab, highlight the first one.
         * If the user selects the tab using an #anchor prefer this tab.
         */
        function registerTabEvents() {
            /**
             * Activates the specified tab.
             *
             * @param {String} selector - selector of the tab
             * @return true if the tab has been selected
             */
            function selectTab(selector) {
                const detailsTabs = $('#tab-details');
                const selectedTab = detailsTabs.find(selector);

                if (selectedTab.length !== 0) {
                    const tab = new bootstrap5.Tab(selectedTab[0]);
                    tab.show();

                    return true;
                }
                return false
            }

            const selectedTabID = 'jenkins-coverage-activeTab';
            const url = document.location.toString();
            if (url.match('#')) {
                window.location.hash = '';
                const tabName = url.split('#')[1];
                if (selectTab('a[data-bs-target="#' + tabName + '"]')) {
                    localStorage.setItem(selectedTabID, '#' + tabName);
                }
            }
            else {
                const activeTab = localStorage.getItem(selectedTabID);
                if (activeTab) {
                    selectTab('a[data-bs-target="' + activeTab + '"]');
                }
            }
            if ($('#tab-details a.active').length === 0) {
                selectTab('li:first-child a'); // fallback if all other options fail
            }

            $('a[data-bs-toggle="tab"]').on('shown.bs.tab', function (e) {
                window.location.hash = e.target.hash;
                const activeTab = $(e.target).attr('data-bs-target');
                localStorage.setItem(selectedTabID, activeTab);
                redrawCharts();
            });
        }

        /**
         * Loads all chart JSON models via AJAX calls from the server and renders the corresponding echarts.
         */
        // TODO: maybe it would make sense to render only visible charts
        function initializeCharts() {
            renderTrendChart();

            viewProxy.getOverview(function (t) {
                createOverview(t.responseObject(), 'coverage-overview');
            });
        }

        function resizeChartOf(selector) {
            $(selector)[0].echart.resize();
        }

        function renderTrendChart() {
            const configuration = JSON.stringify(echartsJenkinsApi.readFromLocalStorage('jenkins-echarts-chart-configuration-coverage-history'));
            viewProxy.getTrendChart(configuration, function (t) {
                echartsJenkinsApi.renderConfigurableZoomableTrendChart('coverage-trend', t.responseJSON, 'chart-configuration-coverage-history', openBuild);
                resizeChartOf('#coverage-trend');
            });
        }

        /**
         * Event handler to resizes all charts.
         */
        function redrawCharts() {
            renderTrendChart(); // re-render since the configuration might have changed

            resizeChartOf('#coverage-overview');
        }

        function registerTrendChartConfiguration() {
            const trendConfigurationDialogId = 'chart-configuration-coverage-history';
            $('#' + trendConfigurationDialogId).on('hidden.bs.modal', function () {
                redrawCharts();
            });
        }

        /**
         * Initializes a selection listener for a datatable which loads the selected source code.
         *
         * @param {String} tableId The ID of the DataTable
         */
        function initializeSourceCodeSelection(tableId) {
            const datatable = $('#' + tableId + '-table-inline').DataTable();
            const sourceView = $('#' + tableId + '-source-file');
            const noFileSelectedBanner = $('#' + tableId + '-no-selection');
            const noSourceAvailableBanner = $('#' + tableId + '-no-source');

            function showNoSelection() {
                sourceView.hide();
                noSourceAvailableBanner.hide();
                noFileSelectedBanner.show();
            }

            function showNoSourceCode() {
                sourceView.hide();
                noFileSelectedBanner.hide();
                noSourceAvailableBanner.show();
            }

            function showSourceCode() {
                noFileSelectedBanner.hide();
                noSourceAvailableBanner.hide();
                sourceView.show();
            }

            showNoSelection();
            datatable.on('select', function (e, dt, type, indexes) {
                if (type === 'row') {
                    showSourceCode();
                    sourceView.html('Loading...');
                    const rowData = datatable.rows(indexes).data().toArray();
                    viewProxy.getSourceCode(rowData[0].fileHash, tableId + '-table', function (t) {
                        const sourceCode = t.responseObject();
                        if (sourceCode === "N/A") {
                            showNoSourceCode();
                        }
                        else {
                            sourceView.html(sourceCode);
                        }
                    });
                }
                else {
                    showNoSelection();
                }
            })
            datatable.on('deselect', function () {
                showNoSelection();
            });
        }

        registerTrendChartConfiguration();
        registerTabEvents();

        initializeCharts();

        window.addEventListener('resize', function () {
            redrawCharts();
        });

        $(document).ready(function () {
            initializeSourceCodeSelection('absolute-coverage');
            initializeSourceCodeSelection('change-coverage');
        });
    }
};
