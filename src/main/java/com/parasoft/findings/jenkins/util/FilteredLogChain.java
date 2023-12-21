package com.parasoft.findings.jenkins.util;

import com.parasoft.findings.jenkins.coverage.ParasoftCoverageRecorder;
import edu.hm.hafner.util.FilteredLog;
import hudson.model.TaskListener;
import io.jenkins.plugins.util.LogHandler;

import java.util.ArrayList;
import java.util.List;

public class FilteredLogChain {
    private final List<FilteredLog> logs = new ArrayList<>();
    private final TaskListener taskListener;

    public FilteredLogChain(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public synchronized FilteredLog addNewFilteredLog(String title) {
        FilteredLog log = new FilteredLog(title);
        logs.add(log);
        return log;
    }

    public synchronized FilteredLog mergeAllLogs() {
        FilteredLog log = new FilteredLog();
        for(FilteredLog existingLog : logs) {
            log.merge(existingLog);
        }
        return log;
    }

    public synchronized LogHandler getLogHandler() {
        return new LogHandler(taskListener, ParasoftCoverageRecorder.PARASOFT_COVERAGE_NAME);
    }
}
