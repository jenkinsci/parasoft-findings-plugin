<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <xsl:variable name="newLine" select="'&#xA;'" />

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="ResultsSession/@toolName='SOAtest'">
                <xsl:apply-templates select="ResultsSession/ExecutedTestsDetails"></xsl:apply-templates>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="ExecutedTestsDetails">
        <xsl:apply-templates select="Total" />
    </xsl:template>

    <!-- ===================== create root testsuites node using 'total' =========== -->
    <xsl:template match="Total">
        <testsuites tests="{@total}" failures="{@fail}" errors="0">
            <xsl:call-template name="timeAttrIfAvailable" />
            <xsl:call-template name="RootTests" />
        </testsuites>
    </xsl:template>

    <!-- ===================== process root test suites =========== -->
    <xsl:template name="RootTests">
        <xsl:variable name="topTest" select="/ResultsSession/ExecutedTestsDetails/Total/Project//*[@root = 'true']" />

        <xsl:for-each select="($topTest)">
            <xsl:call-template name="RootTestInfo">
                <xsl:with-param name="testNode" select="." />
                <xsl:with-param name="depth">0</xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <!-- ===================== process root test - create testsuite for each tst project =========== -->
    <xsl:template name="RootTestInfo">
        <xsl:param name="testNode" />
        <xsl:param name="depth" />

        <xsl:variable name="testID" select="$testNode/@id" />
        <xsl:variable name="isSuite" select="name($testNode) = 'TestSuite'" />

        <xsl:variable name="rootTestName"><xsl:call-template name="parent_path_to_test">
            <xsl:with-param name="dirty_path" select="($testID)" />
        </xsl:call-template></xsl:variable>

        <xsl:variable name="rootTestName1"><xsl:call-template name="replace-last">
            <xsl:with-param name="text" select="$rootTestName" />
            <xsl:with-param name="replace" select="'.'" />
            <xsl:with-param name="by" select="'_'" />
        </xsl:call-template></xsl:variable>
        <xsl:variable name="rootTestName2"><xsl:call-template name="replace-last">
            <xsl:with-param name="text" select="$rootTestName1" />
            <xsl:with-param name="replace" select="'/'" />
            <xsl:with-param name="by" select="'.'" />
        </xsl:call-template></xsl:variable>

        <xsl:variable name="totalTests" select="$testNode/@total" />

        <testsuite name="{$rootTestName2}" tests="{$totalTests}">
            <xsl:call-template name="timeAttrIfAvailable" />

            <xsl:for-each select="./Test[@total > 0] | ./TestSuite[@total > 0]">
                <xsl:call-template name="TestCaseInfo">
                    <xsl:with-param name="testNode" select="." />
                    <xsl:with-param name="depth" select="($depth) + 1" />
                </xsl:call-template>
            </xsl:for-each>
        </testsuite>

    </xsl:template>

    <!-- ===================== process tests - create testcase =========== -->
    <xsl:template name="TestCaseInfo">
        <xsl:param name="testNode" />
        <xsl:param name="depth" />

        <xsl:variable name="isTestSuite" select="name($testNode) = 'TestSuite'" />
        <xsl:variable name="isTest" select="name($testNode) = 'Test'" />
        <xsl:variable name="testID" select="$testNode/@id" />

        <xsl:if test="$isTestSuite">
            <xsl:for-each select="./Test[@total > 0] | ./TestSuite[@total > 0]">
                <xsl:call-template name="TestCaseInfo">
                    <xsl:with-param name="testNode" select="." />
                    <xsl:with-param name="depth" select="($depth) + 1" />
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="$isTest">
            <xsl:variable name="testName"><xsl:call-template name="path_to_test">
                <xsl:with-param name="testNode" select="$testNode" />
                <xsl:with-param name="currentNode" select="$testNode" />
            </xsl:call-template></xsl:variable>

            <xsl:variable name="testCasesNumber" select="count(./TestCase)" />
            <xsl:choose>
                <xsl:when test="$testCasesNumber > 1">
                    <xsl:for-each select="./TestCase">
                        <xsl:variable name="testParams" select="@params" />
                        <xsl:variable name="funcViols" select="/ResultsSession/FunctionalTests/FuncViols/FuncViol[@testCaseId=$testID and @testParams=$testParams]" />

                        <xsl:call-template name="writeXUnitTestCase">
                            <xsl:with-param name="testName" select="concat($testName,'[',@params,']')" />
                            <xsl:with-param name="funcViols" select="$funcViols" />
                            <xsl:with-param name="time" select="@time" />
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="funcViols" select="/ResultsSession/FunctionalTests/FuncViols/FuncViol[@testCaseId=$testID]" />
                    <xsl:call-template name="writeXUnitTestCase">
                        <xsl:with-param name="testName" select="$testName" />
                        <xsl:with-param name="funcViols" select="$funcViols" />
                        <xsl:with-param name="time" select="@time" />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>

    </xsl:template>

    <!-- ===================== get parent path for test =========== -->
    <xsl:template name="parent_path_to_test">
        <xsl:param name="dirty_path" />

        <xsl:variable name="trim_start" select="substring-after(($dirty_path),':///')" />
        <xsl:variable name="clean_path" select="substring-before(($trim_start),'#')" />
        <xsl:choose>
            <xsl:when test="not(string-length($clean_path) > 0)">
                <xsl:value-of select="substring-before(($trim_start),'.')" /> "/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="($clean_path)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ================= get path to test ================== -->
    <xsl:template name="path_to_test">
        <xsl:param name="testNode" />
        <xsl:param name="currentNode" />

        <xsl:if test="(name($currentNode) = 'TestSuite' or name($currentNode) = 'Test') and contains($currentNode/@id, '#')">
            <xsl:call-template name="path_to_test">
                <xsl:with-param name="testNode" select="$testNode" />
                <xsl:with-param name="currentNode" select="$currentNode/.." />
            </xsl:call-template>
            <xsl:value-of select="$currentNode/@name" /><xsl:if test="$currentNode != $testNode">/</xsl:if>
        </xsl:if>
    </xsl:template>

    <!-- ================= add time attribute ================== -->
    <xsl:template name="timeAttrIfAvailable">
        <xsl:if test="string-length(@time) > 0">
            <xsl:attribute name="time">
            <xsl:call-template name="timeFormat">
                <xsl:with-param name="initTime" select="@time" />
            </xsl:call-template>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!-- ================= format time ================== -->
    <xsl:template name="timeFormat">
        <xsl:param name="initTime" />

        <xsl:variable name="millis">
            <xsl:value-of select="substring($initTime,9,3)" />
        </xsl:variable>

        <xsl:variable name="sec">
            <xsl:value-of select="substring($initTime,6,2)" />
        </xsl:variable>

        <xsl:variable name="min">
            <xsl:value-of select="substring($initTime,3,2)" />
        </xsl:variable>

        <xsl:variable name="hour">
            <xsl:value-of select="substring($initTime,1,1)" />
        </xsl:variable>

        <xsl:variable name="allSec">
            <xsl:value-of select="$sec + (60 * $min) + (3600 * $hour)" />
        </xsl:variable>

        <xsl:value-of select="concat($allSec,'.',$millis)" />
    </xsl:template>

    <!-- ================= write xUnit test case ================== -->
    <xsl:template name="writeXUnitTestCase">
        <xsl:param name="testName" />
        <xsl:param name="funcViols" />
        <xsl:param name="time" />

        <xsl:variable name="timeInMillis">
            <xsl:call-template name="timeFormat">
                <xsl:with-param name="initTime" select="$time" />
            </xsl:call-template>
        </xsl:variable>

        <testcase name="{$testName}" time="{$timeInMillis}">
            <xsl:if test="count($funcViols) > 0">
                <xsl:choose>
                    <xsl:when test="count($funcViols) > 1">
                        <xsl:variable name="combinedFailures">
                            <xsl:for-each select="$funcViols">
                                <xsl:if test="position() > 1 and string-length(@msg) > 0">
                                    <xsl:value-of select="concat($newLine, $newLine)" />
                                </xsl:if>
                                <xsl:value-of select="@msg" />
                                <xsl:if test="string-length(@violationDetails) > 0">
                                    <xsl:value-of select="concat($newLine, @violationDetails)" />
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:variable>
                        <failure message="Multiple errors reported">
                            <xsl:value-of select="$combinedFailures" />
                        </failure>
                    </xsl:when>
                    <xsl:otherwise>
                        <failure message="{$funcViols/@msg}">
                            <xsl:if test="string-length($funcViols/@violationDetails) > 0">
                                <xsl:value-of select="$funcViols/@violationDetails" />
                            </xsl:if>
                        </failure>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </testcase>
    </xsl:template>

    <!-- ================= replace the last occurrence of 'replace' to 'by' in 'text' ================== -->
    <xsl:template name="replace-last">
        <xsl:param name="text" />
        <xsl:param name="replace" />
        <xsl:param name="by" />
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">

                <xsl:if test="contains(substring-after($text, $replace), $replace)">
                    <xsl:value-of select="substring-before($text,$replace)" /><xsl:value-of select="$replace" />
                </xsl:if>
                <xsl:if test="not(contains(substring-after($text, $replace), $replace))">
                    <xsl:value-of select="substring-before($text,$replace)" /><xsl:value-of select="$by" />
                </xsl:if>
                <xsl:call-template name="replace-last">
                    <xsl:with-param name="text" select="substring-after($text, $replace)" />
                    <xsl:with-param name="replace" select="$replace" />
                    <xsl:with-param name="by" select="$by" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ================= replace the all occurrences of 'replace' to 'by' in 'text' ================== -->
    <xsl:template name="string-replace-all">
        <xsl:param name="text" />
        <xsl:param name="replace" />
        <xsl:param name="by" />
        <xsl:choose>
            <xsl:when test="$text = '' or $replace = ''or not($replace)">
                <!-- Prevent this routine from hanging -->
                <xsl:value-of select="$text" />
            </xsl:when>
            <xsl:when test="contains($text, $replace)">
                <xsl:value-of select="substring-before($text,$replace)" /><xsl:value-of select="($by)" disable-output-escaping="yes" />
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="text" select="substring-after($text,$replace)" />
                    <xsl:with-param name="replace" select="$replace" />
                    <xsl:with-param name="by" select="$by" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
