<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <xsl:variable name="isLegacyMode" select="count(/ResultsSession/ExecutedTestsDetails) = 1"/>
    <xsl:variable name="isFunctionalResult" select="/ResultsSession/@toolName = 'SOAtest'"/>
    <xsl:variable name="useFullClassName" select="/ResultsSession/@toolName = 'C++test' or /ResultsSession/@toolId='c++test'" />

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="not($isLegacyMode = 'true') and not($isFunctionalResult = 'true')">
                <!--  execution results 10.x  -->
                <xsl:call-template name="processExecutionResults"/>
            </xsl:when>
            <xsl:when test="($isLegacyMode = 'true') and not($isFunctionalResult = 'true')">
                <!--  execution results 9.x  -->
                <xsl:call-template name="processLegacyExecutionResults" />
            </xsl:when>
            <xsl:when test="($isLegacyMode = 'true') and ($isFunctionalResult = 'true')">
                <!--  functional results 9.x  -->
            </xsl:when>
            <xsl:when test="not($isLegacyMode = 'true') and ($isFunctionalResult = 'true')">
                <!--  functional results 10.x  -->
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- =================  Execution results 10.x ================== -->

    <xsl:template name="processExecutionResults">
        <xsl:apply-templates select="/ResultsSession/Exec/ExecutedTestsDetails/Total" mode="exec" />
    </xsl:template>

    <xsl:template match="Total" mode="exec">
        <testsuites>
            <xsl:apply-templates select="Project/TestSuite | TestSuite" mode="exec" />
        </testsuites>
    </xsl:template>

    <xsl:template match="TestSuite" mode="exec">
        <xsl:choose>
            <xsl:when test="parent::TestSuite">
                <xsl:apply-templates select="Test" mode="exec" />
                <xsl:apply-templates select="TestSuite" mode="exec"/>
            </xsl:when>
            <xsl:otherwise>

                <testsuite name="{@name}" tests="{count(.//Test[not(TestCase)] | .//Test/TestCase)}" id="{position()}" package="">
                    <xsl:call-template name="addTimeAttr" />
                    <xsl:call-template name="addHostnameAttr"/>
                    <xsl:call-template name="addTimestampAttr"/>
                    <xsl:call-template name="addFailuresAttr"/>
                    <xsl:call-template name="addErrorsAttr"/>
                    <properties/>
                    <xsl:apply-templates select="Test" mode="exec" />
                    <xsl:apply-templates select="TestSuite" mode="exec" />
                    <system-out/>
                    <system-err/>
                </testsuite>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="Test" mode="exec">
        <xsl:choose>
            <xsl:when test="TestCase">
                <xsl:for-each select="TestCase">
                    <xsl:call-template name="writeXUnitTestCase_Exec">
                        <xsl:with-param name="id" select="@testId" />
                        <xsl:with-param name="status" select="@status" />
                        <xsl:with-param name="tcId" select="@id" />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="writeXUnitTestCase_Exec">
                    <xsl:with-param name="id" select="@id" />
                    <xsl:with-param name="status" select="@status" />
                    <xsl:with-param name="tcId" select="'null'" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="writeXUnitTestCase_Exec">
        <xsl:param name="id" />
        <xsl:param name="status" />
        <xsl:param name="tcId" select="'null'" />

        <xsl:variable name="className">
            <xsl:call-template name="getClassname" />
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$tcId='null'">

                <testcase name="{@name}" classname="{$className}">
                    <xsl:call-template name="addTimeAttr" />
                    <xsl:if test="$status!='pass'">
                        <xsl:call-template name="processExecViols">
                            <xsl:with-param name="execViols" select="/ResultsSession/Exec/ExecViols/ExecViol[@testId=$id]" />
                            <xsl:with-param name="unitViols" select="/ResultsSession/Exec/ExecViols/UnitViol[@testId=$id]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                    </xsl:if>
                </testcase>
            </xsl:when>
            <xsl:otherwise>
                <testcase name="{@name}" classname="{$className}">
                    <xsl:call-template name="addTimeAttr" />
                    <xsl:if test="$status!='pass'">
                        <xsl:call-template name="processExecViols">
                            <xsl:with-param name="execViols" select="/ResultsSession/Exec/ExecViols/ExecViol[@testId=$id and @tcId=$tcId]" />
                            <xsl:with-param name="unitViols" select="/ResultsSession/Exec/ExecViols/UnitViol[@testId=$id and @tcId=$tcId]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                    </xsl:if>
                </testcase>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processExecViols">
        <xsl:param name="execViols" />
        <xsl:param name="unitViols" />
        <xsl:param name="status" />

        <xsl:choose>
            <xsl:when test="$status='err'">
                <error type="Error">
                    <xsl:call-template name="printUnitViols">
                        <xsl:with-param name="unitViols" select="$unitViols" />
                    </xsl:call-template>
                    <xsl:call-template name="printExecViols">
                        <xsl:with-param name="execViols" select="$execViols" />
                    </xsl:call-template>
                </error>
            </xsl:when>
            <xsl:when test="$status='fail'">
                <failure type="Failure">
                    <xsl:call-template name="printUnitViols">
                        <xsl:with-param name="unitViols" select="$unitViols" />
                    </xsl:call-template>
                    <xsl:call-template name="printExecViols">
                        <xsl:with-param name="execViols" select="$execViols" />
                    </xsl:call-template>
                </failure>
            </xsl:when>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="printUnitViols">
        <xsl:param name="unitViols" />
        <xsl:if test="count($unitViols) > 0">
            <xsl:call-template name="processStackTrace">
                <xsl:with-param name="unitViols" select="$unitViols" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="processStackTrace">
        <xsl:param name="unitViols"/>
        <xsl:for-each select="$unitViols/Thr/ThrPart">
            <xsl:sort select="position()" data-type="number" order="descending" />
            <xsl:if test="position() != '1'">
                <xsl:text>&#xa;&#x9;</xsl:text>
                <xsl:text>Caused by: </xsl:text>
            </xsl:if>
            <xsl:value-of select="concat(@type,': ',@msg)" />

            <xsl:for-each select="PathElem">
                <xsl:text>&#xa;&#x9;</xsl:text>
                <xsl:text>at </xsl:text>
                <xsl:value-of select="@desc" />
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="printExecViols">
        <xsl:param name="execViols" />

        <xsl:if test="count($execViols) > 0">
            <xsl:choose>
                <xsl:when test="count($execViols) > 1">
                    <xsl:variable name="combinedFailures">
                        <xsl:for-each select="$execViols">
                            <xsl:if test="position() > 1 and string-length(@msg) > 0">
                                <xsl:text>&#xa;&#x9;</xsl:text>
                                <xsl:text>&#xa;&#x9;</xsl:text>
                            </xsl:if>
                            <xsl:value-of select="@msg" />
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:attribute name="message">Multiple errors reported</xsl:attribute>
                    <xsl:value-of select="$combinedFailures" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="message"><xsl:value-of select="$execViols/@msg" /></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- =================  Execution results 9.x ================== -->

    <xsl:template name="processLegacyExecutionResults">
        <xsl:apply-templates select="/ResultsSession/ExecutedTestsDetails/Total" mode="execLegacy" />
    </xsl:template>

    <xsl:template match="Total" mode="execLegacy">
        <testsuites>
            <xsl:apply-templates select="Project/TestSuite | TestSuite" mode="execLegacy" />
        </testsuites>
    </xsl:template>

    <xsl:template match="TestSuite" mode="execLegacy">
        <xsl:choose>
            <xsl:when test="parent::TestSuite">
                <xsl:apply-templates select="Test" mode="execLegacy" />
                <xsl:apply-templates select="TestSuite" mode="execLegacy"/>
            </xsl:when>
            <xsl:otherwise>

                <testsuite name="{@name}" tests="{count(.//Test[not(TestCase)] | .//Test/TestCase)}"
                           id="{position()}" package="">
                    <xsl:call-template name="addTimeAttr" />
                    <xsl:call-template name="addHostnameAttr"/>
                    <xsl:call-template name="addTimestampAttr"/>
                    <xsl:call-template name="addFailuresAttr"/>
                    <xsl:call-template name="addErrorsAttr"/>
                    <properties/>
                    <xsl:apply-templates select="Test" mode="execLegacy" />
                    <xsl:apply-templates select="TestSuite" mode="execLegacy" />
                    <system-out/>
                    <system-err/>
                </testsuite>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getStatus">
        <xsl:param name="testNode"/>

        <xsl:if test="($testNode/@err) > 0">err</xsl:if>
        <xsl:if test="not(($testNode/@err) > 0) and ($testNode/@fail) > 0">fail</xsl:if>
        <xsl:if test="not(($testNode/@err) > 0) and not(($testNode/@fail) > 0) and ($testNode/@pass) > 0">pass</xsl:if>
    </xsl:template>

    <xsl:template match="Test" mode="execLegacy">
        <xsl:choose>
            <xsl:when test="TestCase">
                <xsl:for-each select="TestCase">
                    <xsl:variable name="status">
                        <xsl:call-template name="getStatus">
                            <xsl:with-param name="testNode" select="." />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:call-template name="writeXUnitTestCase_ExecLegacy">
                        <xsl:with-param name="id" select="@testId" />
                        <xsl:with-param name="status" select="$status" />
                        <xsl:with-param name="tcId" select="@id" />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="status">
                    <xsl:call-template name="getStatus">
                        <xsl:with-param name="testNode" select="." />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="writeXUnitTestCase_ExecLegacy">
                    <xsl:with-param name="id" select="@id" />
                    <xsl:with-param name="status" select="$status" />
                    <xsl:with-param name="tcId" select="'null'" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="writeXUnitTestCase_ExecLegacy">
        <xsl:param name="id" />
        <xsl:param name="status" />
        <xsl:param name="tcId" select="'null'" />

        <xsl:variable name="className">
            <xsl:call-template name="getClassname" />
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$tcId='null'">
                <testcase name="{@name}" classname="{$className}">
                    <xsl:call-template name="addTimeAttr" />
                    <xsl:if test="$status!='pass'">
                        <xsl:call-template name="processExecViol_Legacy">
                            <xsl:with-param name="execViols" select="/ResultsSession/Exec/ExecViols/ExecViol[@testId=$id]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                    </xsl:if>
                </testcase>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="testName" select="ancestor::Test[position()=1]/@name" />
                <testcase name="{$testName}[{@params}]" classname="{$className}">
                    <xsl:call-template name="addTimeAttr" />
                    <xsl:if test="$status!='pass'">
                        <xsl:call-template name="processExecViol_Legacy">
                            <xsl:with-param name="execViols" select="/ResultsSession/Exec/ExecViols/ExecViol[@testId=$id and @testCaseId=$tcId]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                    </xsl:if>
                </testcase>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processExecViol_Legacy">
        <xsl:param name="execViols" />
        <xsl:param name="status" />
        <xsl:choose>
            <xsl:when test="$status='err'">
                <error type="Error">
                    <xsl:text>&#xa;&#x9;</xsl:text>
                    <xsl:call-template name="processStackTrace_Legacy">
                        <xsl:with-param name="execViols" select="$execViols" />
                    </xsl:call-template>
                </error>
            </xsl:when>
            <xsl:when test="$status='fail'">
                <failure type="Failure">
                    <xsl:text>&#xa;&#x9;</xsl:text>
                    <xsl:call-template name="processStackTrace_Legacy">
                        <xsl:with-param name="execViols" select="$execViols" />
                    </xsl:call-template>
                </failure>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processStackTrace_Legacy">
        <xsl:param name="execViols"/>
        <xsl:for-each select="$execViols">
            <xsl:sort select="@ln" />
            <xsl:if test="position() > 1">
                <xsl:text>&#xa;&#x9;</xsl:text>
                <xsl:text>&#xa;&#x9;</xsl:text>
            </xsl:if>
            <xsl:for-each select="./Thr/ThrPart">
                <xsl:sort select="position()" data-type="number" order="descending" />
                <xsl:if test="position() != '1'">
                    <xsl:text>&#xa;&#x9;</xsl:text>
                    <xsl:text>Caused by: </xsl:text>
                </xsl:if>
                <xsl:value-of select="(@prnMsg)" />

                <xsl:for-each select="Trace">
                    <xsl:text>&#xa;&#x9;</xsl:text>
                    <xsl:text>at </xsl:text>
                    <xsl:if test="string-length(@ln) > 0">
                        <xsl:value-of select="concat(@fileName,':',@ln)" />
                    </xsl:if>
                    <xsl:if test="not(string-length(@ln) > 0)">
                        <xsl:value-of select="(@fileName)" />
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <!-- =================  Utilities ================== -->

    <xsl:template name="addTimeAttr">
        <xsl:choose>
            <xsl:when test="string-length(@time) > 0">
                <xsl:attribute name="time">
                    <xsl:call-template name="timeFormat">
                        <xsl:with-param name="initTime" select="@time" />
                    </xsl:call-template>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="time">
                    <xsl:value-of select="0"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

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

    <xsl:template name="getClassname">
        <xsl:choose>
            <xsl:when test="$useFullClassName">
                <xsl:call-template name="getFullClassName">
                    <xsl:with-param name="currentNode"
                                    select="ancestor::TestSuite[position()=1]" />
                    <xsl:with-param name="depth">0</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="ancestor::TestSuite[position()=1]/@name" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getFullClassName">
        <xsl:param name="currentNode" />
        <xsl:param name="depth" />

        <xsl:if
                test="(name($currentNode) = 'TestSuite' or name($currentNode) = 'Project')">
            <xsl:call-template name="getFullClassName">
                <xsl:with-param name="currentNode"
                                select="$currentNode/.." />
                <xsl:with-param name="depth" select="($depth) + 1" />
            </xsl:call-template>

            <xsl:value-of select="$currentNode/@name" />

            <xsl:if test="$depth = 1">
                <xsl:text>.</xsl:text>
            </xsl:if>
            <xsl:if test="$depth > 1">
                <xsl:text>/</xsl:text>
            </xsl:if>
        </xsl:if>

    </xsl:template>

    <xsl:template name="addHostnameAttr">
        <xsl:attribute name="hostname">
            <xsl:call-template name="getAttrValueOrDefault">
                <xsl:with-param name="attr" select="/ResultsSession/@machine"/>
                <xsl:with-param name="defaultValue">-</xsl:with-param>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:template>

    <xsl:template name="addTimestampAttr">
        <xsl:attribute name="timestamp">
            <xsl:variable name="dateAntJunitFormat" select="substring(/ResultsSession/@time, 1, 19)"/>
            <xsl:choose>
                <xsl:when test="string-length($dateAntJunitFormat) = 19">
                    <xsl:value-of select="$dateAntJunitFormat"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'1111-11-11T00:00:00'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
    </xsl:template>

    <xsl:template name="addFailuresAttr">
        <xsl:attribute name="failures">
            <xsl:call-template name="getAttrValueOrDefault">
                <xsl:with-param name="attr" select="@fail"/>
                <xsl:with-param name="defaultValue">0</xsl:with-param>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:template>

    <xsl:template name="addErrorsAttr">
        <xsl:attribute name="errors">
            <xsl:call-template name="getAttrValueOrDefault">
                <xsl:with-param name="attr" select="@err"/>
                <xsl:with-param name="defaultValue">0</xsl:with-param>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:template>

    <xsl:template name="getAttrValueOrDefault">
        <xsl:param name="attr" />
        <xsl:param name="defaultValue" />

        <xsl:choose>
            <xsl:when test="$attr != ''">
                <xsl:value-of select="$attr"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$defaultValue"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>