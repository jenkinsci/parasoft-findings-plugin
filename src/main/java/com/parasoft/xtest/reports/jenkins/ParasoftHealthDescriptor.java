/*
 * $Id$
 *
 * (C) Copyright Parasoft Corporation 2013. All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
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
