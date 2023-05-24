/*
 * Copyright 2017 Parasoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parasoft.findings.jenkins.util;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.remoting.RoleChecker;

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
     * @param file the file to check 
     * @return true if file behind given object is absolute
     */
    public static boolean isAbsolute(FilePath file)
    {
        boolean result = false;
        try {
            result = file.act(new IsAbsoluteFileCallable());
        } catch (IOException e) {
            Logger.getLogger().errorTrace(e);
        } catch (InterruptedException e) {
            Logger.getLogger().errorTrace(e);
        }
        return result;
    }
    
    /**
     * @param file the file with properties to load
     * @return properties loaded from given file
     */
    public static Properties loadProperties(FilePath file)
    {
        Properties props = null;
        try {
            props = file.act(new LoadPropertiesFileCallable());
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

    private static final class IsAbsoluteFileCallable implements FileCallable<Boolean> {

        private static final long serialVersionUID = 1L;

        public Boolean invoke(File f, VirtualChannel channel)
                throws IOException, InterruptedException
        {
            return f.isAbsolute();
        }

        @Override
        public void checkRoles(RoleChecker arg0)
                throws SecurityException
        {
            // TODO Auto-generated method stub

        }
    }

    private static final class LoadPropertiesFileCallable implements FileCallable<Properties> {

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

        @Override
        public void checkRoles(RoleChecker arg0)
                throws SecurityException
        {
            // TODO Auto-generated method stub

        }
    }
}
    
