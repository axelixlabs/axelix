package com.sivalabs.ft.notifications.quartz

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

/**
 * Logs each execution so the scheduler's execution counter.
 */
class LoggingQuartzJob : Job {

    override fun execute(context: JobExecutionContext) {
        log.info("Quartz playground job executed by {}", context.fireInstanceId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(LoggingQuartzJob::class.java)
    }
}
