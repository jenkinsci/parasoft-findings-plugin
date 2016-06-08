/*
 * Copyright 2016 Parasoft Corporation
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

package com.parasoft.xtest.reports.jenkins;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.util.model.AnnotationProvider;

import org.jvnet.localizer.Localizable;

import com.parasoft.xtest.common.nls.NLS;

/**
 * A health descriptor for Parasoft build results.
 */
public class ParasoftHealthDescriptor
    extends AbstractHealthDescriptor
{
    /** Unique ID of this class. */
    private static final long serialVersionUID = 4577565119607932230L;

    /**
     * Creates a new instance of {@link ParasoftHealthDescriptor} based on the values of the
     * specified descriptor.
     * 
     * @param healthDescriptor the descriptor to copy the values from
     */
    public ParasoftHealthDescriptor(final HealthDescriptor healthDescriptor)
    {
        super(healthDescriptor);
    }

    
    @Override
    protected Localizable createDescription(final AnnotationProvider result)
    {
        if (result.getNumberOfAnnotations() == 0) {
            return new LocalizableString(Messages.PARASOFT_RESULT_ACTION_HEALTH_REPORT_NO_ITEM);
        } else if (result.getNumberOfAnnotations() == 1) {
            return new LocalizableString(Messages.PARASOFT_RESULT_ACTION_HEALTH_REPORT_SINGLE_ITEM);
        } else {
            String sMessage = NLS.getFormatted(Messages.PARASOFT_RESULT_ACTION_HEALTH_REPORT_MULTIPLE_ITEM, result.getNumberOfAnnotations());
            return new LocalizableString(sMessage);
        }
    }
}
