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

package com.parasoft.findings.jenkins.coverage.api.metrics.source;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.coverage.model.FileNode;
import edu.hm.hafner.util.ResourceTest;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for {@link SourceCodeFacade}.
 *
 * @author Florian Orendi
 */
class SourceCodeFacadeTest extends ResourceTest {

    private static final String WHOLE_SOURCE_CODE = "SourcecodeTest.html";
    private static final String MODIFIED_LINES_COVERAGE_SOURCE_CODE = "SourcecodeTestCC.html";

    @Test
    void shouldCalculateSourcecodeForModifiedLinesCoverage() throws IOException {
        SourceCodeFacade sourceCodeFacade = createSourceCodeFacade();
        String originalHtml = readHtml(WHOLE_SOURCE_CODE);
        FileNode node = createFileCoverageNode();

        String requiredHtml = Jsoup.parse(readHtml(MODIFIED_LINES_COVERAGE_SOURCE_CODE), Parser.xmlParser()).html();

        String modifiedLinesCoverageHtml =
                sourceCodeFacade.calculateModifiedLinesCoverageSourceCode(originalHtml, node);
        assertThat(modifiedLinesCoverageHtml).isEqualTo(requiredHtml);
    }

    /**
     * Creates an instance of {@link SourceCodeFacade}.
     *
     * @return the created instance
     */
    private SourceCodeFacade createSourceCodeFacade() {
        return new SourceCodeFacade();
    }

    private FileNode createFileCoverageNode() {
        FileNode file = new FileNode("", "path");
        List<Integer> lines = Arrays.asList(10, 11, 12, 16, 17, 18, 19);
        for (Integer line : lines) {
            file.addModifiedLines(line);
        }
        file.addIndirectCoverageChange(6, -1);
        file.addIndirectCoverageChange(7, -1);
        file.addIndirectCoverageChange(14, 1);
        file.addIndirectCoverageChange(15, 1);
        for (int i = 1; i <= 25; i++) {
            file.addCounters(i, 1, 0);
        }
        return file;
    }

    /**
     * Reads a sourcecode HTML file for testing.
     *
     * @param name
     *         The name of the file
     *
     * @return the file content
     * @throws IOException
     *         if reading failed
     */
    private String readHtml(final String name) throws IOException {
        return new String(Files.readAllBytes(getResourceAsFile(name)));
    }
}
