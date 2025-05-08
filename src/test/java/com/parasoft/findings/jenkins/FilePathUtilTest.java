
package com.parasoft.findings.jenkins;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Properties;

import com.parasoft.findings.jenkins.util.FilePathUtil;

import org.junit.jupiter.api.Test;

import hudson.FilePath;

class FilePathUtilTest {

    @Test
    void isAbsoluteTest() {
        assertFalse(FilePathUtil.isAbsolute(new FilePath(
                new File("src/test/resources/xml"))));
        String path = new File("src/test/resources/rule/APSC_DV.000160.SRD.html").getAbsolutePath();
        assertTrue(FilePathUtil.isAbsolute(new FilePath(
                new File(path))));
    }

    @Test
    void loadPropertiesTest() {
        assertEquals(new Properties(), FilePathUtil.loadProperties(
                new FilePath(new File("src/test/resources/empty.properties"))));
    }
}
