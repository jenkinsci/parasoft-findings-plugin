/*
 * Copyright 2023 Parasoft Corporation
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

package com.parasoft.findings.jenkins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

public final class FileUtil
{

    /**
	 * Private constructor to prevent instantiation.
	 */
    private FileUtil() { }

    /**
	 * Returns local storage directory for all files.
	 * @param props
	 * @return
	 *
	 */
    public static File getLocalStorageDir(Properties props)
    {
        return LoggingStorageUtil.getLocalStorageDir(props);
    }

    public static File getTempDir()
            throws IOException
    {
        File configDir = getLocalStorageDir(new Properties());
        File tempDir = new File(configDir, "temp"); //$NON-NLS-1$
        tempDir.mkdirs();
        if (!(tempDir.isDirectory())) {
            String sErrorMessage = "Failed to create temporary directory at " + tempDir.getPath(); //$NON-NLS-1$
            Logger.getLogger().error(sErrorMessage);
            throw new IOException(sErrorMessage);
        }
        if (!(tempDir.canWrite())) {
            String sErrorMessage = "Temporary directory is not writable at " + tempDir.getPath(); //$NON-NLS-1$
            Logger.getLogger().error(sErrorMessage);
            throw new IOException(sErrorMessage);
        }
        return tempDir;
    }

    /**
     * Delete given path.
     * If it is a file - remove sole file.
     * If it is a directory - remove it along with all removable contents.
     * @param root what to delete
     *
     * @pre root != null
     */
    public static boolean recursiveDelete(File root)
    {
        if (!root.exists()){
            return true;
        }

        if (root.isDirectory()){
            File [] listing = root.listFiles();
            for (File element : listing) {
                recursiveDelete(element);
            }
        }

        return root.delete();
    }

    /**
     * This utility creates and returns local storage directory where all temporary
     * application specific data is kept.
     */
    public static final class LoggingStorageUtil
    {
        public static final String LOCAL_STORAGE_DIR = "local.storage.dir"; //$NON-NLS-1$

        /** System property used to configure local storage dir */
        public static final String LOCAL_STORAGE_DIR_SYSTEM_PROPERTY = "parasoft." + LOCAL_STORAGE_DIR; //$NON-NLS-1$

        private LoggingStorageUtil() { }

        /**
         * Try to resolve location for local storage directory base on system property or provided settings.
         * @param properties
         * @return String resolved storage location or null
         * @pre properties != null
         */
        public static String resolveLocalStorageDir(Properties properties)
        {
            String sStorageDir = System.getProperty(LOCAL_STORAGE_DIR_SYSTEM_PROPERTY);
            if ((sStorageDir == null) || (sStorageDir.trim().length() <= 0)) {
                // Try to resolve local storage directory from local settings
                sStorageDir = properties.getProperty(LOCAL_STORAGE_DIR);
            }
            return sStorageDir;
        }

        /**
         * Returns local storage directory for all temporary files.
         * @param settings local settings
         * @return local storage directory
         * @pre localSettings != null
         * @post $result != null
         */
        public static File getLocalStorageDir(Properties settings)
        {
            String sStorageDir = resolveLocalStorageDir(settings);
            if ((sStorageDir != null) && (sStorageDir.trim().length() > 0)) {
                File storageDir = new File(sStorageDir);
                if (checkStorageDir(storageDir)) {
                    return storageDir;
                }
            }
            if (TEMP_STORAGE_DIR != null) {
                return TEMP_STORAGE_DIR;
            }
            try {
                Thread.sleep(500); // parasoft-suppress BD.TRS.TSHL "reviewed"
            } catch (InterruptedException ie) {
                // do nothing
            }
            sStorageDir = resolveLocalStorageDir(settings);
            if ((sStorageDir != null) && (sStorageDir.trim().length() > 0)) {
                File storageDir = new File(sStorageDir);
                if (checkStorageDir(storageDir)) {
                    return storageDir;
                }
            }
            // Logger.getLogger().error("Can not resolve local storage directory base on system property nor settings.");  //$NON-NLS-1$
            File storageDir = getTempLocalStorageDir();
            // we always return temp storage dir
            checkStorageDir(storageDir);
            TEMP_STORAGE_DIR = storageDir;
            return storageDir;
        }

        private static boolean checkStorageDir(File storageDir)
        {
            storageDir = storageDir.getAbsoluteFile();
            storageDir.mkdirs();
            if (!(storageDir.isDirectory())) {
                Logger.getLogger().error("Failed to create local storage directory at " + storageDir.getPath()); //$NON-NLS-1$
                return false;
            }
            if (!(storageDir.canWrite())) {
                Logger.getLogger().error("Local storage directory is not writable at " + storageDir.getPath()); //$NON-NLS-1$
                return false;
            }
            return true;
        }

        private synchronized static File getTempLocalStorageDir()
        {
            File parasoftDir = getTempParasoftDir();
            File storagesDir = new File(parasoftDir, STORAGES_DIR_NAME);

            clearOldSession(storagesDir, (dir, name) -> dir.isDirectory() && name.startsWith(STORAGE_DIR_PREFIX), NO_OF_STORAGES_TO_PRESERVE);
            return new File(storagesDir, STORAGE_DIR_PREFIX + getLogDatePart());
        }

        public static String getLogDatePart()
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); //$NON-NLS-1$
            return simpleDateFormat.format(new Date());
        }

        /**
         * Allows to clear old session data located in session directory.
         * @param sessionDir
         * @param filter
         * @param noOfSessionToPreserve
         */
        public static void clearOldSession(File sessionDir, FilenameFilter filter, int noOfSessionToPreserve)
        {
            String[] asList = sessionDir.list(filter);
            if ((asList == null) || (asList.length <= noOfSessionToPreserve)) {
                return;
            }
            Arrays.sort(asList);
            int toDelete = asList.length - noOfSessionToPreserve;
            for(int i = 0; i != toDelete; i++) {
                File dirToDelete = new File(sessionDir.getAbsolutePath() + File.separator + asList[i]);
                if (canDelete(dirToDelete)) {
                    deleteDirectoryRecursive(dirToDelete);
                }
            }
        }

        private static File getTempParasoftDir()
        {
            String sSystemTempDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
            String sSystemUser = System.getProperty("user.name"); //$NON-NLS-1$

            String sParasoftDirName = PARASOFT_NAME;
            if (!isWindowsOS() && (sSystemUser != null)) {
                sParasoftDirName = (PARASOFT_NAME + '-' + sSystemUser);
            }

            File parasoftDir = null;
            if (sSystemTempDir != null) {
                parasoftDir = new File(sSystemTempDir, sParasoftDirName);
            } else {
                Logger.getLogger().error("Cannot find a temporary directory, set the java.io.tmpdir system property appropriately."); //$NON-NLS-1$
                parasoftDir = new File(sParasoftDirName);
            }
            return parasoftDir;
        }

        /**
         * Check if directory can be deleted. The criterion is the
         * modified time of the most recent file.
         * @param dirToDelete the candidate directory for deletion
         * @return true if directory can be deleted or false in other case
         */
        private static boolean canDelete(File dirToDelete)
        {
            File[] files = dirToDelete.listFiles();
            if ((files == null) || (files.length == 0)) {
                return true;
            }
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            Date lastModiffied = new Date(files[0].lastModified());
            Date currentTime = new Date();
            Date isOld = new Date(currentTime.getTime() - Duration.ofDays(IS_OLD).toMillis());

            return lastModiffied.before(isOld);
        }

        private static void deleteDirectoryRecursive(File dir)
        {
            File[] listFiles = dir.listFiles();
            if (listFiles == null) {
                return;
            }
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursive(file);
                    continue;
                }
                file.delete();
            }
            dir.delete();
        }

        /** Temporary storage directory */
        private static File TEMP_STORAGE_DIR = null;

        private static boolean isWindowsOS()
        {
            return File.separatorChar == '\\';
        }

        private final static String PARASOFT_NAME = "parasoft"; //$NON-NLS-1$

        private final static int NO_OF_STORAGES_TO_PRESERVE = 10;
        private final static String STORAGES_DIR_NAME = "storages"; //$NON-NLS-1$
        private final static String STORAGE_DIR_PREFIX = "storage_"; //$NON-NLS-1$

        /** Files created IS_OLD days ago are considered old */
        private final static int IS_OLD = 2;
    }
}