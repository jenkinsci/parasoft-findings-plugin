/*
 * $Id: Messages.java 221698 2012-01-31 10:27:19Z maco $
 *
 * (C) Copyright ParaSoft Corporation 2010. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.xtest.reports.jenkins.dashboard;

import com.parasoft.xtest.common.nls.NLS;

/**
 * Provides localized messages for this package.
 */
final class Messages
    extends NLS
{
    static {
        // initialize resource bundle
        NLS.initMessages(Messages.class);
    }

    /**
     * Just to prevent instantiation.
     */
    private Messages() {}

    public static String PORTLET_WARNINGS_TABLE;

    public static String PORTLET_WARNINGS_PRIORITY_GRAPH;

    public static String PORTLET_WARNINGS_NEW_VS_FIXED_GRAPH;

    public static String PORTLET_WARNINGS_TOTALS_GRAPH;
	
} // final class Messages