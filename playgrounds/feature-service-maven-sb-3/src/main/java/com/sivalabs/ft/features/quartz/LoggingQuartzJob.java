package com.sivalabs.ft.features.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs each execution so the scheduler's execution counter.
 */
public class LoggingQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(LoggingQuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Quartz playground job executed by {}", context.getFireInstanceId());
    }
}
