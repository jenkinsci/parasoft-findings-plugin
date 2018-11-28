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

package com.parasoft.xtest.reports.jenkins.html;

public interface IHtmlTags
{

    String ITALIC_END_TAG = "</i>"; //$NON-NLS-1$

    String ITALIC_START_TAG = "<i>"; //$NON-NLS-1$

    String BOLD_END_TAG = "</b>"; //$NON-NLS-1$

    String BOLD_START_TAG = "<b>"; //$NON-NLS-1$

    String CODE_END_TAG = "</code>"; //$NON-NLS-1$

    String CODE_START_TAG = "<code>"; //$NON-NLS-1$

    String NON_BREAKABLE_SPACE = "&nbsp"; //$NON-NLS-1$

    String NON_BREAKABLE_4_SPACE = NON_BREAKABLE_SPACE + NON_BREAKABLE_SPACE + NON_BREAKABLE_SPACE + NON_BREAKABLE_SPACE;

    String DIAMOND_SEPARATOR = " &diams; "; //$NON-NLS-1$

    String LIST_END_TAG = "</ul>"; //$NON-NLS-1$

    String LIST_START_TAG = "<ul>"; //$NON-NLS-1$

    String LIST_ELEM_END_TAG = "</li>"; //$NON-NLS-1$

    String LIST_ELEM_START_TAG = "<li>"; //$NON-NLS-1$
    
    String SPAN_COLOR_START_TAG = "<span style=\"color:"; //$NON-NLS-1$
    
    String  SPAN_COLOR_CLOSE_START_TAG = "\">";

    String SPAN_END_TAG = "</span>"; //$NON-NLS-1$

    String FONT_MONOSPACE_SPAN_START_TAG = "<span style='font-family: monospace;'>"; //$NON-NLS-1$

    String HEADER_START_TAG = "<h1>"; //$NON-NLS-1$

    String HEADER_END_TAG = "</h1>"; //$NON-NLS-1$
    
    String BREAK_LINE_TAG = "</br>"; //$NON-NLS-1$

}
