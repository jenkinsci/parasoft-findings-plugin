package com.parasoft.xtest.reports.jenkins.tool;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.parasoft.xtest.reports.jenkins.parser.ParasoftParser;

import edu.hm.hafner.analysis.IssueParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

public class ParasoftTool
extends ReportScanningTool
{
    @DataBoundConstructor
    public ParasoftTool()
    {
        super();
    }

    @Override
    public IssueParser createParser()
    {
        return new ParasoftParser();
    }

    @DataBoundSetter
    public void setSettingsFile(final String settingsFile)
    {
        // TODO - set settings file for parser
    }

    @Extension
    public static class Descriptor
    extends ReportScanningToolDescriptor
    {
        public Descriptor()
        {
            super("parasoft-plugin");
        }

        @Override
        public String getDisplayName()
        {
            return "Parasoft Findings";
        }

        @Override
        public boolean canScanConsoleLog()
        {
            return false;
        }
    }

}
