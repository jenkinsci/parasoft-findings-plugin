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

package com.parasoft.findings.jenkins.parser;

import java.util.HashMap;

public class ParasoftIssueAdditionalProperties // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
        extends HashMap<String, Object>
{
    private static final long serialVersionUID = -5014146322978138084L;

    public ParasoftIssueAdditionalProperties()
    {}

    public ParasoftIssueAdditionalProperties(String author, String revision, String analyzer)
    {
        put(AUTHOR_KEY, author);
        put(REVISION_KEY, revision);
        put(ANALYZER_KEY, analyzer);
    }

    public String getAuthor()
    {
        return (String)get(AUTHOR_KEY);
    }

    public String getRevision()
    {
        return (String)get(REVISION_KEY);
    }

    public String getAnalyzer()
    {
        return (String)get(ANALYZER_KEY);
    }

    public static final String ANALYZER_KEY = "analyzer"; //$NON-NLS-1$
    public static final String REVISION_KEY = "revision"; //$NON-NLS-1$
    public static final String AUTHOR_KEY = "author"; //$NON-NLS-1$
}
