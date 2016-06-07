/*
 * $Id: Messages.java 221698 2012-01-31 10:27:19Z maco $
 *
 * (C) Copyright ParaSoft Corporation 2010. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.xtest.reports.jenkins.internal.variables;

import com.parasoft.xtest.common.nls.NLS;

/**
 * The resourcer for this package.
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
    private Messages()
    {}

    public static String PERCENT_SIGN_MISSING;

    public static String CURLY_BRACKET_MISSING;

} // final class Messages