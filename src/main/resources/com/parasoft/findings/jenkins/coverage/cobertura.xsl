<?xml version="1.0" encoding="UTF-8"  standalone="yes"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map">
    <xsl:variable name="toolName" select="/Coverage/@toolId"/>
    <xsl:variable name="toolVer" select="/Coverage/@toolVer"/>
    <xsl:variable name="toolDispName" select="/Coverage/@toolDispName"/>
    <xsl:param name="pipelineBuildWorkingDirectory"><xsl:value-of select="/Coverage/@pipelineBuildWorkingDirectory"/></xsl:param>

    <xsl:template name="getUriWithoutFilePrefix">
        <xsl:param name="rawUri"/>
        <xsl:choose>
            <!-- for file:///xxx uri pattern -->
            <xsl:when test="matches($rawUri, '^file:///([^/\\]+)')">
                <xsl:variable name="uriWithoutFilePrefix" select="substring-after($rawUri, 'file:///')"/>
                <xsl:call-template name="makeUriSystemCompatible">
                    <xsl:with-param name="uriWithoutFilePrefix" select="$uriWithoutFilePrefix"/>
                </xsl:call-template>
            </xsl:when>
            <!-- for file://hostname/xxx uri pattern -->
            <xsl:when test="matches($rawUri, '^file://([^/]+)/([^/\\]+)')">
                <!-- Extract the hostname from an uri, like: the result is 'hostname' for uri 'file://hostname/C:/abc' -->
                <xsl:variable name="hostname" select="replace($rawUri, '^file://([^/]+)(/.*)$', '$1')" />
                <xsl:variable name="uriWithoutFilePrefix" select="substring-after($rawUri, concat('file://', $hostname , '/'))"/>
                <xsl:call-template name="makeUriSystemCompatible">
                    <xsl:with-param name="uriWithoutFilePrefix" select="$uriWithoutFilePrefix"/>
                </xsl:call-template>
            </xsl:when>
            <!-- for file:/ uri pattern -->
            <xsl:when test="matches($rawUri, '^file:/([^/\\]+)')">
                <xsl:variable name="uriWithoutFilePrefix" select="substring-after($rawUri, 'file:/')"/>
                <xsl:call-template name="makeUriSystemCompatible">
                    <xsl:with-param name="uriWithoutFilePrefix" select="$uriWithoutFilePrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$rawUri"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="makeUriSystemCompatible">
        <xsl:param name="uriWithoutFilePrefix"/>
        <xsl:choose>
            <!-- on windows: start with X:/ or X:\ -->
            <xsl:when test="matches($uriWithoutFilePrefix, '^[A-Za-z]:[/\\]')">
                <xsl:value-of select="$uriWithoutFilePrefix"/>
            </xsl:when>
            <!-- on linux -->
            <xsl:otherwise>
                <xsl:value-of select="concat('/', $uriWithoutFilePrefix)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/">
        <xsl:element name="coverage">
            <xsl:variable name="allLocNodes" select="/Coverage/Locations/Loc"/>
            <xsl:variable name="linesValid">
                <xsl:call-template name="getCoverableLineNumber">
                    <xsl:with-param name="locNodesToCalcute" select="$allLocNodes"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$linesValid > 0">
                <xsl:variable name="linesCovered">
                    <xsl:call-template name="getCoveredLineNumber">
                        <xsl:with-param name="locNodesToCalcute" select="$allLocNodes"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:attribute name="line-rate">
                    <xsl:value-of select="$linesCovered div $linesValid"/>
                </xsl:attribute>
                <xsl:attribute name="lines-covered">
                    <xsl:value-of select="$linesCovered"/>
                </xsl:attribute>
                <xsl:attribute name="lines-valid">
                    <xsl:value-of select="$linesValid"/>
                </xsl:attribute>
                <xsl:attribute name="version">
                    <xsl:value-of select="$toolDispName"/><xsl:text> </xsl:text><xsl:value-of select="$toolVer"/>
                </xsl:attribute>
                <xsl:call-template name="packages"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template name="packages">
        <xsl:element name="packages">
            <!-- Group by the parent path of uri-->
            <xsl:for-each-group select="/Coverage/Locations/Loc" group-by="substring-before(@uri, tokenize(@uri, '/')[last()])">
                <xsl:variable name="theFirstUriWithoutFilePrefixInCurrentGroup">
                    <xsl:call-template name="getUriWithoutFilePrefix">
                        <xsl:with-param name="rawUri" select="@uri"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="lineRateForPacakgeTag">
                    <xsl:call-template name="getLineRateForPackage">
                        <xsl:with-param name="locNodesToCalcute" select="current-group()"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="$lineRateForPacakgeTag != -1">
                    <xsl:element name="package">
                        <xsl:variable name="uncodedPipelineBuildWorkingDirectory">
                            <xsl:if test="string($pipelineBuildWorkingDirectory) != ''">
                                <xsl:value-of select="concat(translate($pipelineBuildWorkingDirectory, '\', '/'), '/')"/>
                            </xsl:if>
                        </xsl:variable>
                        <xsl:variable name="encodedPipelineBuildWorkingDirectory">
                            <xsl:if test="string($uncodedPipelineBuildWorkingDirectory) != ''">
                                <!-- Replace % with %25 and space with %20 to get an encoded path-->
                                <xsl:value-of select="replace(replace($uncodedPipelineBuildWorkingDirectory, '%', '%25'), ' ', '%20')"/>
                            </xsl:if>
                        </xsl:variable>
                        <xsl:variable name="processedPipelineBuildWorkingDirectory">
                            <xsl:choose>
                                <xsl:when test="string($uncodedPipelineBuildWorkingDirectory) != '' and contains($theFirstUriWithoutFilePrefixInCurrentGroup, $uncodedPipelineBuildWorkingDirectory)">
                                    <xsl:value-of select="$uncodedPipelineBuildWorkingDirectory"/>
                                </xsl:when>
                                <!-- Using encoded pipeline build working directory when the uri attribute of <Loc> tag in Parasoft tool report(e.g. jtest report) is encoded -->
                                <xsl:when test="string($encodedPipelineBuildWorkingDirectory) != '' and contains($theFirstUriWithoutFilePrefixInCurrentGroup, $encodedPipelineBuildWorkingDirectory)">
                                    <xsl:value-of select="$encodedPipelineBuildWorkingDirectory"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="''"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="isExternalReport" select="$processedPipelineBuildWorkingDirectory = ''"/>
                        <xsl:variable name="packageName">
                            <xsl:choose>
                                <xsl:when test="$isExternalReport">
                                    <xsl:call-template name="getPackageName">
                                        <xsl:with-param name="projectPath" select="$theFirstUriWithoutFilePrefixInCurrentGroup"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="getPackageName">
                                        <!-- Get relative source file path -->
                                        <xsl:with-param name="projectPath" select="substring-after($theFirstUriWithoutFilePrefixInCurrentGroup, $processedPipelineBuildWorkingDirectory)"/>
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:attribute name="name">
                            <xsl:value-of select="$packageName"/>
                        </xsl:attribute>
                        <xsl:attribute name="line-rate">
                            <xsl:value-of select="$lineRateForPacakgeTag"/>
                        </xsl:attribute>
                        <xsl:element name="classes">
                            <xsl:for-each select="current-group()">
                                <xsl:variable name="sourceFileUriWithoutFilePrefix">
                                    <xsl:call-template name="getUriWithoutFilePrefix">
                                        <xsl:with-param name="rawUri" select="@uri"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="filePath">
                                    <xsl:choose>
                                        <xsl:when test="$isExternalReport">
                                            <!-- Replace %25 with % and %20 with space to get an uncoded path-->
                                            <xsl:value-of select="replace(replace($sourceFileUriWithoutFilePrefix, '%25', '%'), '%20', ' ')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <!-- Get relative source file path -->
                                            <!-- Replace %25 with % and %20 with space to get an uncoded path-->
                                            <xsl:value-of select="replace(replace(substring-after($sourceFileUriWithoutFilePrefix, $processedPipelineBuildWorkingDirectory), '%25', '%'), '%20', ' ')"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:variable>
                                <xsl:variable name="locRef" select="@locRef"/>
                                <xsl:variable name="cvgDataNode" select="//CvgData[@locRef=$locRef]"/>
                                <xsl:choose>
                                    <xsl:when test="$toolName = 'jtest'">
                                        <xsl:variable name="typeItemNodes" select="$cvgDataNode/Stats//Item[has-children()]"/>
                                        <xsl:for-each select="$typeItemNodes">
                                            <xsl:variable name="methodItemRefsUnderCurrentType" select="./Item[not(has-children())]/@itemRef"/>
                                            <xsl:variable name="className">
                                                <xsl:call-template name="getDisplayClassNameForJava">
                                                    <xsl:with-param name="packageName" select="$packageName"/>
                                                    <xsl:with-param name="originalClassName" select="@name"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:call-template name="generateClassElementByItemRefs">
                                                <xsl:with-param name="itemRefs" select="$methodItemRefsUnderCurrentType"/>
                                                <xsl:with-param name="cvgDataNode" select="$cvgDataNode"/>
                                                <xsl:with-param name="className" select="$className"/>
                                                <xsl:with-param name="filePath" select="$filePath"/>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:variable name="allItemRefsUnderCurrentFile" select="$cvgDataNode/Stats//Item/@itemRef"/>
                                        <xsl:variable name="className">
                                            <xsl:call-template name="getClassNameForDotNetAndCpp">
                                                <xsl:with-param name="filePath" select="$filePath"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:call-template name="generateClassElementByItemRefs">
                                            <xsl:with-param name="itemRefs" select="$allItemRefsUnderCurrentFile"/>
                                            <xsl:with-param name="cvgDataNode" select="$cvgDataNode"/>
                                            <xsl:with-param name="className" select="$className"/>
                                            <xsl:with-param name="filePath" select="$filePath"/>
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
            </xsl:for-each-group>
        </xsl:element>
    </xsl:template>

    <xsl:template name="getLineRateForPackage">
        <xsl:param name="locNodesToCalcute"/>
        <xsl:variable name="linesValid">
            <xsl:call-template name="getCoverableLineNumber">
                <xsl:with-param name="locNodesToCalcute" select="$locNodesToCalcute"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$linesValid > 0">
                <xsl:variable name="linesCovered">
                    <xsl:call-template name="getCoveredLineNumber">
                        <xsl:with-param name="locNodesToCalcute" select="$locNodesToCalcute"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="$linesCovered div $linesValid"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="-1"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getCoverableLineNumber">
        <xsl:param name="locNodesToCalcute"/>
        <xsl:variable name="linesValid" as="xs:integer*">
            <xsl:for-each select="$locNodesToCalcute">
                <xsl:variable name="lineNumbers" as="xs:string*">
                    <xsl:variable name="locRefValue" select="@locRef"/>
                    <xsl:variable name="statCvgElems" select="string-join(/Coverage/CoverageData/CvgData[@locRef = $locRefValue]/Static/StatCvg/@elems, ' ')"/>
                    <xsl:sequence select="distinct-values(tokenize($statCvgElems, '\s+'))"/>
                </xsl:variable>
                <xsl:sequence select="count($lineNumbers)"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:value-of select="sum($linesValid)"/>
    </xsl:template>

    <xsl:template name="getCoveredLineNumber">
        <xsl:param name="locNodesToCalcute"/>
        <xsl:variable name="linesCovered" as="xs:integer*">
            <xsl:for-each select="$locNodesToCalcute">
                <xsl:variable name="coveredLineNumbers" as="xs:string*">
                    <xsl:variable name="locRefValue" select="@locRef"/>
                    <xsl:variable name="coveredLinesSeq" as="xs:string*">
                        <xsl:for-each select="/Coverage/CoverageData/CvgData[@locRef = $locRefValue]/Dynamic//DynCvg">
                            <xsl:sequence select="string(string-join(.//CtxCvg/@elemRefs, ' '))"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:sequence select="tokenize(string-join($coveredLinesSeq, ' '), '\s+')"/>
                </xsl:variable>
                <xsl:sequence select="count(distinct-values($coveredLineNumbers))"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:value-of select="sum($linesCovered)"/>
    </xsl:template>

    <xsl:template name="getPackageName">
        <xsl:param name="projectPath"/>
        <xsl:variable name="delimiter" select="'/'"/>
        <xsl:variable name="segments" select="tokenize($projectPath, '/')"/>
        <xsl:choose>
            <xsl:when test="count($segments) > 1">
                <xsl:variable name="filename">
                    <xsl:value-of select="$segments[last()]"/>
                </xsl:variable>
                <xsl:choose>
                    <!--    Jtest    -->
                    <xsl:when test="$toolName = 'jtest'">
                        <xsl:variable name="packageNamePrefix">
                            <xsl:call-template name="getPackageNamePrefix">
                                <xsl:with-param name="projId" select="@projId"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="contains($projectPath, translate($packageNamePrefix, '.', '/'))">
                                <xsl:variable name="formattedResourceProjectPath" select="replace(substring-before($projectPath, concat($delimiter, $filename)), $delimiter, '.')"/>
                                <xsl:value-of select="substring-after($formattedResourceProjectPath, substring-before($formattedResourceProjectPath, $packageNamePrefix))"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'&lt;default&gt;'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <!--     Dottest  or CPPTest std     -->
                    <xsl:when test="$toolName = 'dottest' or $toolName = 'c++test'">
                        <!-- Replace %25 with % and %20 with space to get an uncoded path-->
                        <xsl:value-of select="replace(replace(substring-before($projectPath, concat($delimiter, $filename)), '%25', '%'), '%20', ' ')"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'&lt;none&gt;'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getPackageNamePrefix">
        <xsl:param name="projId"/>
        <xsl:choose>
            <xsl:when test="contains($projId, ':')">
                <xsl:value-of select="substring-before($projId, ':')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$projId"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getClassNameForDotNetAndCpp">
        <xsl:param name="filePath"/>
        <xsl:variable name="fileName" select="tokenize($filePath, '/')[last()]"/>
        <xsl:value-of select="$fileName"/>
    </xsl:template>

    <xsl:template name="generateClassElementByItemRefs">
        <!-- All itemRef from /CoverageData/CvgData/Stats/Item which belong to the current class -->
        <xsl:param name="itemRefs"/>
        <xsl:param name="cvgDataNode"/>
        <xsl:param name="className"/>
        <xsl:param name="filePath"/>

        <!-- Generate string which contains unique line numbers of the current class (referenced by itemRefs) -->
        <xsl:variable name="itemRefsString" select="concat(' ', string-join($itemRefs, ' '), ' ')"/>
        <xsl:variable name="statCvgElems" select="$cvgDataNode/Static/StatCvg[contains($itemRefsString, concat(' ', @itemRef, ' '))]/@elems"/>
        <xsl:variable name="statCvgElemsString" select="string-join($statCvgElems, ' ')"/>
        <xsl:variable name="lineNumbers" select="distinct-values(tokenize($statCvgElemsString, '\s+'))"/>

        <!-- This map is used to store unique line numbers. key: line number, value: covered times -->
        <xsl:variable name="linesMap" as="map(xs:string, xs:integer)">
            <xsl:map>
                <xsl:for-each select="$lineNumbers">
                    <xsl:variable name="lineCoveredTimes">
                        <xsl:call-template name="getLineCoveredTimes">
                            <xsl:with-param name="cvgDataNode" select="$cvgDataNode"/>
                            <xsl:with-param name="lineNumber" select="."/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:map-entry key="string(.)" select="xs:integer($lineCoveredTimes)"/>
                </xsl:for-each>
            </xsl:map>
        </xsl:variable>

        <xsl:variable name="verboseLineNumbers" select="tokenize($statCvgElemsString, '\s+')"/>
        <xsl:variable name="statCvgHashes" select="$cvgDataNode/Static/StatCvg[contains($itemRefsString, concat(' ', @itemRef, ' '))]/@hashes"/>
        <xsl:variable name="lineHashes" select="tokenize(string-join($statCvgHashes, ' '), '\s+')"/>

        <!-- This variable is used to store the lineNumber(@elems) and lineHash(@hashes) pair according to the index -->
        <xsl:variable name="lineHashPairs">
            <xsl:for-each select="$verboseLineNumbers">
                <xsl:variable name="index" select="position()"/>
                <xsl:variable name="lineHash" select="$lineHashes[$index]"/>
                <xsl:element name="lineHashPair">
                    <xsl:attribute name="lineNumber" select="string(.)"/>
                    <xsl:attribute name="lineHash" select="string($lineHash)" />
                </xsl:element>
            </xsl:for-each>
        </xsl:variable>
        <!-- This map is used to store unique line numbers and it's line hash. key: line number, value: line hash -->
        <xsl:variable name="lineHashesMap" as="map(xs:string, xs:string)">
            <xsl:map>
                <xsl:for-each select="$lineNumbers">
                    <xsl:variable name="line" select="."/>
                    <xsl:variable name="lineHash" select="$lineHashPairs/lineHashPair[@lineNumber=$line]/@lineHash"/>
                    <xsl:map-entry key="string(.)" select="string($lineHash[1])"/>
                </xsl:for-each>
            </xsl:map>
        </xsl:variable>

        <xsl:if test="count(map:keys($linesMap)) > 0">
            <xsl:element name="class">
                <xsl:attribute name="filename">
                    <xsl:value-of select="$filePath"/>
                </xsl:attribute>
                <xsl:attribute name="name">
                    <xsl:value-of select="$className"/>
                </xsl:attribute>
                <xsl:attribute name="line-rate">
                    <!-- This map is used to store unique covered line numbers. key: covered line number, value: dummy value which is not used -->
                    <xsl:variable name="coveredLinesMap" as="map(xs:string, xs:integer)">
                        <xsl:map>
                            <xsl:for-each select="map:keys($linesMap)">
                                <xsl:variable name="lineNumber" select="."/>
                                <xsl:variable name="lineCoveredTimes">
                                    <xsl:value-of select="map:get($linesMap, $lineNumber)"/>
                                </xsl:variable>
                                <xsl:if test="$lineCoveredTimes > 0">
                                    <xsl:map-entry key="string(.)" select="1"/>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:map>
                    </xsl:variable>
                    <!-- line-rate = # of coverable lines / # of covered line -->
                    <xsl:value-of select="count(map:keys($coveredLinesMap)) div count(map:keys($linesMap))"/>
                </xsl:attribute>
                <xsl:element name="lines">
                    <xsl:for-each select="map:keys($linesMap)">
                        <xsl:sort data-type="number"/>
                        <xsl:element name="line">
                            <xsl:variable name="lineNumber" select="."/>
                            <xsl:attribute name="number">
                                <xsl:value-of select="$lineNumber"/>
                            </xsl:attribute>
                            <xsl:attribute name="hits">
                                <xsl:value-of select="map:get($linesMap, $lineNumber)"/>
                            </xsl:attribute>
                            <xsl:attribute name="hash">
                                <xsl:value-of select="map:get($lineHashesMap, $lineNumber)"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:for-each>
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="getDisplayClassNameForJava">
        <xsl:param name="packageName"/>
        <xsl:param name="originalClassName"/>
        <xsl:choose>
            <xsl:when test="$packageName != '&lt;default&gt;'">
                <xsl:value-of select="concat($packageName, '.', translate($originalClassName, '$', '#'))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="translate($originalClassName, '$', '#')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getLineCoveredTimes">
        <xsl:param name="cvgDataNode"/>
        <xsl:param name="lineNumber"/>
        <!-- Leading and trailing spaces (' ') around the search string are included to avoid incorrect matches when using 'contains()' -->
        <xsl:variable name="ctxCvgNodes" select="$cvgDataNode/Dynamic/DynCvg/CtxCvg[contains(concat(' ', @elemRefs, ' '), concat(' ', $lineNumber, ' '))]"/>
        <xsl:value-of select="count(tokenize(string-join($ctxCvgNodes/@testRefs, ' '), '\s+'))"/>
    </xsl:template>
</xsl:stylesheet>