package com.parasoft.findings.jenkins.coverage;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ProcessedFileResultTest {

    @Test
    public void testProcessFileResult() {
        String coberturaPattern = "coberturaPattern";
        String generatedCoverageBuildDir = "generatedCoverageBuildDir";
        ProcessedFileResult processedFileResult = new ProcessedFileResult(coberturaPattern, generatedCoverageBuildDir);

        assertEquals(coberturaPattern, processedFileResult.getCoberturaPattern());
        assertEquals(generatedCoverageBuildDir, processedFileResult.getGeneratedCoverageBuildDir());
    }
}
