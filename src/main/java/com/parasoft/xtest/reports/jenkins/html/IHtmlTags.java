/*
* (C) Copyright ParaSoft Corporation 2013. All rights reserved.
* THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
* The copyright notice above does not evidence any
* actual or intended publication of such source code.
*/


package com.parasoft.xtest.reports.jenkins.html;


public interface IHtmlTags
{

    String ITALIC_END_TAG = "</i>"; //$NON-NLS-1$

    String GRAY_ITALIC_STYLE_START_TAG = "<i style=\"color:#B0B0B0;\">"; //$NON-NLS-1$

    String BOLD_END_TAG = "</b>"; //$NON-NLS-1$

    String BOLD_START_TAG = "<b>"; //$NON-NLS-1$

    String CODE_END_TAG = "</code>"; //$NON-NLS-1$

    String CODE_START_TAG = "<code>"; //$NON-NLS-1$

    String NON_BREAKABLE_SPACE = "&nbsp"; //$NON-NLS-1$

    String LINE_SEPARATOR = "    "; //$NON-NLS-1$

    String DIAMOND_SEPARATOR = " &diams; "; //$NON-NLS-1$

    String LIST_END_TAG = "</ul>"; //$NON-NLS-1$

    String LIST_START_TAG = "<ul>"; //$NON-NLS-1$

    String LIST_ELEM_END_TAG = "</li>"; //$NON-NLS-1$

    String LIST_ELEM_START_TAG = "<li>"; //$NON-NLS-1$

    String SPAN_END_TAG = "</span>"; //$NON-NLS-1$

    String FONT_MONOSPACE_SPAN_START_TAG = "<span style='font-family: monospace;'>"; //$NON-NLS-1$

    String HEADER_START_TAG = "<h1>"; //$NON-NLS-1$

    String HEADER_END_TAG = "</h1>"; //$NON-NLS-1$

}
