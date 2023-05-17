/*
 * Copyright 2019 Parasoft Corporation
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

package com.parasoft.findings.jenkins.tool;

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

    public static String AUTHOR_COLUMN_HEADER;

    public static String REVISION_COLUMN_HEADER;

    public static String DETAILS_COLUMN_HEADER;

    public static String FILE_COLUMN_HEADER;

    public static String PACKAGE_COLUMN_HEADER;

    public static String CATEGORY_COLUMN_HEADER;

    public static String TYPE_COLUMN_HEADER;

    public static String SEVERITY_COLUMN_HEADER;

    public static String AGE_COLUMN_HEADER;

    public static String RULE_DOCUMENTATION_UNAVAILABLE;

    public static String PARASOFT_TOOL_DISPLAY_NAME;

    public static String PARASOFT_NAME;

}