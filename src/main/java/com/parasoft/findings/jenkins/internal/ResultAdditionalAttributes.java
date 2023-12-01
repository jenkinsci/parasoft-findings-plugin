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

package com.parasoft.findings.jenkins.internal;

import com.parasoft.findings.utils.results.violations.IRuleViolation;
import com.parasoft.findings.utils.results.violations.ViolationRuleUtil;
import com.parasoft.findings.utils.results.xml.IXmlTagsAndAttributes;

/**
 * Provides additional attributes associated with violation, which are not attached directly to violation itself.
 */
public class ResultAdditionalAttributes
{

    private final IRuleViolation _violation;

    /**
     * @param violation for which to read extra attributes
     */
    public ResultAdditionalAttributes(IRuleViolation violation)
    {
        _violation = violation;
    }

    /**
     * @return true if violation is suppressed and should not be visible
     */
    public boolean isSuppressed()
    {
        String sSuppressionType = _violation.getAttribute(IXmlTagsAndAttributes.SUPPRESSION_TYPE_ATTR);
        return sSuppressionType != null;
    }

    /**
     * @return violation severity
     */
    public int getSeverity()
    {
        return ViolationRuleUtil.getSeverity(_violation);
    }

    /**
     * @return title of violated rule
     */
    public String getRuleTitle()
    {
        return ViolationRuleUtil.getRuleTitle(_violation);
    }

    /**
     * @return category of violated rule
     */
    public String getRuleCategory()
    {
        return ViolationRuleUtil.getRuleCategory(_violation);
    }

    /**
     * @return assigned author
     */
    public String getAuthor()
    {
        return _violation.getAttribute(IXmlTagsAndAttributes.AUTHOR_V2_ATTR);
    }

    /**
     * @return file revision for which this violation has been reported
     */
    public String getRevision()
    {
        return _violation.getAttribute(IXmlTagsAndAttributes.REVISION_ATTR);
    }

}
