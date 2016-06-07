/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */
package com.parasoft.xtest.reports.jenkins.views;

import com.parasoft.xtest.logging.api.ParasoftLogger;

final class Logger
{
    /**
     * Provides logger for this package.
     * @return the logger instance
     *
     * @post $result != null
     */
    public static ParasoftLogger getLogger()
    {
        return _LOGGER;

    } // getLogger()

    /** 
     * Just to prevent instantiation. 
     */
    private Logger()
    {
        super();
        
    } // Logger()
    
    private final static ParasoftLogger _LOGGER = ParasoftLogger.getLogger(Logger.class);
    
} // class Logger
