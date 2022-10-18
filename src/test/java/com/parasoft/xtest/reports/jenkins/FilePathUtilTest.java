
package com.parasoft.xtest.reports.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import com.parasoft.xtest.reports.jenkins.util.FilePathUtil;

import hudson.FilePath;

public class FilePathUtilTest
{
    @Test
    public void isAbsoluteTest()
    {
        assertFalse(FilePathUtil.isAbsolute(new FilePath(
            new File("src/test/resources/xml"))));
        assertTrue(FilePathUtil.isAbsolute(new FilePath(
            new File("C:/test/path/src/test/resources/xml"))));
    }

    @Test
    public void loadPropertiesTest()
    {
        assertEquals(new Properties(), FilePathUtil.loadProperties(
            new FilePath(new File("src/test/resources/empty.properties"))));
    }
}
