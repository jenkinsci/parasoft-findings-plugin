/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins.util;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/** 
 * Utility class for operations related to {@link FilePath}.
 */
public final class FilePathUtil
{

	/**
	 * Private constructor to prevent class instantiation.
	 */
	private FilePathUtil () {}

    /**
     * @param file
     * @return true if file behind given object is absolute
     */
    public static boolean isAbsolute(FilePath file)
    {
        boolean result = false;
        try {
            result = file.act(new FileCallable<Boolean>()
            {
                private static final long serialVersionUID = 1L;

                public Boolean invoke(File f, VirtualChannel channel)
                    throws IOException, InterruptedException
                {
                    return f.isAbsolute();
                }
            });
        } catch (IOException e) {
            Logger.getLogger().errorTrace(e);
        } catch (InterruptedException e) {
            Logger.getLogger().errorTrace(e);
        }
        return result;
    }
    
    /**
     * @param file
     * @return properties loaded from given file
     */
    public static Properties loadProperties(FilePath file)
    {
        Properties props = null;
        try {
            props = file.act(new FileCallable<Properties>()
            {
                private static final long serialVersionUID = -286350596197180650L;

                public Properties invoke(File f, VirtualChannel channel)
                    throws IOException, InterruptedException
                {
                    Logger.getLogger().info("File path is " + f.getAbsolutePath());   //$NON-NLS-1$
                    InputStream input = new FileInputStream(f);
                    try {
                        Properties properties = new Properties();
                        properties.load(input);
                        return properties;
                    } finally {
                        IOUtils.closeQuietly(input);
                    }
                }
            });
        } catch (IOException e) {
            Logger.getLogger().error("Localsettings file not found", e); //$NON-NLS-1$
        } catch (InterruptedException e) {
            Logger.getLogger().error("Error while reading remote file", e); //$NON-NLS-1$
        }
        if (props == null) {
            props = new Properties();
            Logger.getLogger().info("No properties loaded"); //$NON-NLS-1$
        }
        return props;
    }
}
    
