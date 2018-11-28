/*
* (C) Copyright ParaSoft Corporation 2018. All rights reserved.
* THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
* The copyright notice above does not evidence any
* actual or intended publication of such source code.
*/


package com.parasoft.xtest.reports.jenkins.html;


public abstract class Colors
{
    public static final String BLUE = "#1569C7"; // $NON-NLS-1$
    
    public static final String RED = "#B71C1C"; // $NON-NLS-1$
    
    public static final String GREEN = "#008000"; // $NON-NLS-1$

    public static final String GRAY = "#808080"; // $NON-NLS-1$

    public static final String BLACK = "#000000"; // $NON-NLS-1$
    
    public static String createColorSpanStartTag(String color)
    {
        return IHtmlTags.SPAN_COLOR_START_TAG + color + IHtmlTags.SPAN_COLOR_CLOSE_START_TAG;
    }
}
