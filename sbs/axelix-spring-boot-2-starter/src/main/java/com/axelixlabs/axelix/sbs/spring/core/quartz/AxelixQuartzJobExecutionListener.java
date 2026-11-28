/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.quartz;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.EverythingMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;

import com.axelixlabs.axelix.common.contract.metadata.QuartzJobExecutionHistory;

/**
 * Registers with Quartz schedulers and records completed job executions.
 *
 * @author Nikita Kirillov
 */
public class AxelixQuartzJobExecutionListener implements JobListener, InitializingBean {

    /**
     * The whole execution history is reported to master repeatedly, so error messages are bounded.
     */
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private static final Logger logger = LoggerFactory.getLogger(AxelixQuartzJobExecutionListener.class);
    private static final String LISTENER_NAME = "axelixQuartzExecutionHistoryListener";

    private final List<Scheduler> schedulers;
    private final QuartzExecutionHistory history;

    public AxelixQuartzJobExecutionListener(List<Scheduler> schedulers, QuartzExecutionHistory history) {
        this.schedulers = schedulers;
        this.history = history;
    }

    @Override
    public void afterPropertiesSet() {
        for (Scheduler scheduler : schedulers) {
            try {
                scheduler.getListenerManager().addJobListener(this, EverythingMatcher.allJobs());
            } catch (SchedulerException e) {
                logger.warn("Could not register Quartz execution history listener", e);
            }
        }
    }

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {}

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {}

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        try {
            Scheduler scheduler = context.getScheduler();
            String schedulerName = scheduler.getSchedulerName();
            QuartzJobExecutionHistory execution = new QuartzJobExecutionHistory()
                    .executionId(context.getFireInstanceId())
                    .schedulerName(schedulerName)
                    .jobName(context.getJobDetail().getKey().getName())
                    .jobGroup(context.getJobDetail().getKey().getGroup())
                    .triggerName(context.getTrigger().getKey().getName())
                    .triggerGroup(context.getTrigger().getKey().getGroup())
                    .startedAt(context.getFireTime().toInstant().toString())
                    .durationMillis(context.getJobRunTime())
                    .success(jobException == null)
                    .errorType(jobException != null ? jobException.getClass().getName() : null)
                    .errorMessage(jobException != null ? truncateErrorMessage(jobException.getMessage()) : null);
            history.record(schedulerName, execution);
        } catch (SchedulerException e) {
            logger.warn("Could not record completed Quartz job execution", e);
        }
    }

    private static @Nullable String truncateErrorMessage(@Nullable String message) {
        return message != null && message.length() > MAX_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH)
                : message;
    }
}
