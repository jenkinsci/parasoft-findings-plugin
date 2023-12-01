
package com.parasoft.findings.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import com.parasoft.findings.jenkins.util.FilePathUtil;

import hudson.FilePath;

public class FilePathUtilTest
{
    @Test
    public void isAbsoluteTest()
    {
        assertFalse(FilePathUtil.isAbsolute(new FilePath(
                new File("src/test/resources/xml"))));
        String path = new File("src/test/resources/rule/APSC_DV.000160.SRD.html").getAbsolutePath();
        assertTrue(FilePathUtil.isAbsolute(new FilePath(
                new File(path))));
    }

    @Test
    public void loadPropertiesTest()
    {
        assertEquals(new Properties(), FilePathUtil.loadProperties(
                new FilePath(new File("src/test/resources/empty.properties"))));
    }
}
