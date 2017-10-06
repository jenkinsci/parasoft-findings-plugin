<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" />
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="ResultsSession/@toolName!='SOAtest'">
                <xsl:apply-templates select="ResultsSession/Exec"></xsl:apply-templates>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="Exec">
        <xsl:apply-templates select="ExecutedTestsDetails" />
    </xsl:template>

    <xsl:template match="ExecutedTestsDetails">
        <xsl:apply-templates select="Total" />
    </xsl:template>

    <xsl:template match="Total">
        <testsuites tests="{@total}" failures="{@fail}" errors="{@err}">
            <xsl:call-template name="timeAttrIfAvailable" />
            <xsl:apply-templates select="Project/TestSuite" />
        </testsuites>
    </xsl:template>

    <xsl:template match="TestSuite">
        <xsl:choose>
            <xsl:when test="parent::TestSuite">
                <xsl:apply-templates select="Test" />
                <xsl:apply-templates select="TestSuite" />
            </xsl:when>
            <xsl:otherwise>
                <testsuite name="{@name}" tests="{count(.//Test[not(TestCase)] | .//Test/TestCase)}">
                    <xsl:call-template name="timeAttrIfAvailable" />
                    <xsl:apply-templates select="Test" />
                    <xsl:apply-templates select="TestSuite" />
                </testsuite>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="timeAttrIfAvailable">
        <xsl:if test="string-length(@time) > 0">
            <xsl:attribute name="time">
                 <xsl:call-template name="timeFormat">
                     <xsl:with-param name="initTime" select="@time" />
                 </xsl:call-template>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Test">
        <xsl:choose>
            <xsl:when test="TestCase">
                <xsl:for-each select="TestCase">
                    <xsl:call-template name="writeXUnitTestCase">
                        <xsl:with-param name="id" select="@testId" />
                        <xsl:with-param name="status" select="@status" />
                        <xsl:with-param name="tcId" select="@id" />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="writeXUnitTestCase">
                    <xsl:with-param name="id" select="@id" />
                    <xsl:with-param name="status" select="@status" />
                    <xsl:with-param name="tcId" select="'null'" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="writeXUnitTestCase">
        <xsl:param name="id" />
        <xsl:param name="status" />
        <xsl:param name="tcId" select="'null'" />

        <xsl:variable name="className" select="ancestor::TestSuite[position()=1]/@name" />

        <xsl:choose>
            <xsl:when test="$tcId='null'">
                <testcase name="{@name}" classname="{$className}">
                    <xsl:call-template name="timeAttrIfAvailable" />
                    <xsl:if test="$status!='pass'">
                        <xsl:call-template name="UnitViol">
                            <xsl:with-param name="unitViol" select="/ResultsSession/Exec/ExecViols/UnitViol[@testId=$id]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                        <xsl:call-template name="ExecViol">
                            <xsl:with-param name="execViol" select="/ResultsSession/Exec/ExecViols/ExecViol[@testId=$id]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                    </xsl:if>
                </testcase>
            </xsl:when>
            <xsl:otherwise>
                <testcase name="{@name}" classname="{$className}">
                    <xsl:call-template name="timeAttrIfAvailable" />
                    <xsl:if test="$status!='pass'">
                        <xsl:call-template name="UnitViol">
                            <xsl:with-param name="unitViol" select="/ResultsSession/Exec/ExecViols/UnitViol[@testId=$id and @tcId=$tcId]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                        <xsl:call-template name="ExecViol">
                            <xsl:with-param name="execViol" select="/ResultsSession/Exec/ExecViols/ExecViol[@testId=$id and @tcId=$tcId]" />
                            <xsl:with-param name="status" select="$status" />
                        </xsl:call-template>
                    </xsl:if>
                </testcase>
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

    <xsl:template name="UnitViol">
        <xsl:param name="unitViol" />
        <xsl:param name="status" />
        <xsl:if test="count($unitViol) > 0">
            <xsl:choose>
                <xsl:when test="$status='err'">
                    <error>
                        <xsl:text>&#xa;&#x9;</xsl:text>
                        <xsl:call-template name="stack_trace">
                            <xsl:with-param name="unitViol" select="$unitViol" />
                        </xsl:call-template>
                    </error>
                </xsl:when>
                <xsl:when test="$status='fail'">
                    <failure>
                        <xsl:text>&#xa;&#x9;</xsl:text>
                        <xsl:call-template name="stack_trace">
                            <xsl:with-param name="unitViol" select="$unitViol" />
                        </xsl:call-template>
                    </failure>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template name="stack_trace">
        <xsl:param name="unitViol"></xsl:param>
        <xsl:for-each select="$unitViol/Thr/ThrPart">
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

    <xsl:template name="ExecViol">
        <xsl:param name="execViol" />
        <xsl:param name="status" />
        <xsl:if test="count($execViol) > 0">
            <xsl:choose>
                <xsl:when test="$status='err'">
                    <error>
                        <xsl:value-of select="@msg"></xsl:value-of>
                    </error>
                </xsl:when>
                <xsl:when test="$status='fail'">
                    <failure>
                        <xsl:value-of select="@msg"></xsl:value-of>
                    </failure>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>