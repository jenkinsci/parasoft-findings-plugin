/*
 * $Id: Messages.java 221698 2012-01-31 10:27:19Z maco $
 *
 * (C) Copyright ParaSoft Corporation 2010. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.xtest.reports.jenkins;

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
    private Messages() { }

    public static String PARASOFT_PUBLISHER_NAME;

    public static String PARASOFT_PROJECT_ACTION_NAME;

    public static String PARASOFT_TREND_NAME;

    public static String PARASOFT_RESULT_ACTION_HEADER;

    public static String PARASOFT_RESULT_ACTION_HEALTH_REPORT_NO_ITEM;

    public static String PARASOFT_RESULT_ACTION_HEALTH_REPORT_SINGLE_ITEM;

    public static String PARASOFT_RESULT_ACTION_HEALTH_REPORT_MULTIPLE_ITEM;

    public static String COLLECTING_REPORT_FILES;

} // final class Messages