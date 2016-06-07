/*
* (C) Copyright ParaSoft Corporation 2013. All rights reserved.
* THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
* The copyright notice above does not evidence any
* actual or intended publication of such source code.
*/


package com.parasoft.xtest.reports.jenkins;

import java.util.Locale;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;


public class LocalizableString
    extends Localizable
{
    private static final long serialVersionUID = 5885639086662718677L;

    private String _sMessage = null;

    public LocalizableString(ResourceBundleHolder holder, String key, Object... args)
    {
        super(holder, key, args);
    }

    public LocalizableString(String message)
    {
        super(null, null);
        _sMessage = message;
    }

    @Override
    public String toString()
    {
        if (_sMessage != null) {
            return _sMessage;
        }
        return super.toString();
    }

    @Override
    public String toString(Locale locale)
    {
        if (_sMessage != null) {
            return _sMessage;
        }
        return super.toString(locale);
    }

}
