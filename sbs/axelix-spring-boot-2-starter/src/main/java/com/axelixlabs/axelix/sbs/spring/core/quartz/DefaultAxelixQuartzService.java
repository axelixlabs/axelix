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

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobExecution;
import com.axelixlabs.axelix.common.contract.quartz.QuartzSchedulerMetadata;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTrigger;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzJobs;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzSchedulers;

/**
 * Default implementation of {@link AxelixQuartzService}.
 *
 * @author Nikita Kirillov
 */
public class DefaultAxelixQuartzService implements AxelixQuartzService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAxelixQuartzService.class);

    private final List<Scheduler> schedulers;
    private final AxelixQuartzAssembler quartzAssembler;

    public DefaultAxelixQuartzService(List<Scheduler> schedulers, AxelixQuartzAssembler quartzAssembler) {
        this.schedulers = schedulers;
        this.quartzAssembler = quartzAssembler;
    }

    @Override
    public ServiceQuartzSchedulers getQuartzSchedulers() {
        List<QuartzSchedulerMetadata> schedulerMetadata = new ArrayList<>();
        for (Scheduler scheduler : schedulers) {
            try {
                schedulerMetadata.add(quartzAssembler.assembleSchedulerMetadata(scheduler.getMetaData()));
            } catch (SchedulerException e) {
                logger.warn("Could not retrieve Quartz scheduler metadata", e);
            }
        }
        return new ServiceQuartzSchedulers().schedulers(schedulerMetadata);
    }

    @Override
    public @Nullable ServiceQuartzJobs getQuartzJobs(String schedulerName) {
        for (Scheduler scheduler : schedulers) {
            if (schedulerName.equals(getSchedulerName(scheduler))) {
                return getSchedulerJobs(scheduler);
            }
        }
        return null;
    }

    @Override
    public boolean executeQuartzJob(String schedulerName, String jobName, String jobGroup) {
        for (Scheduler scheduler : schedulers) {
            if (schedulerName.equals(getSchedulerName(scheduler))) {
                try {
                    JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
                    if (!scheduler.checkExists(jobKey)) {
                        return false;
                    }
                    scheduler.triggerJob(jobKey);
                    return true;
                } catch (SchedulerException e) {
                    throw new QuartzAccessException("Could not request Quartz job execution", e);
                }
            }
        }
        return false;
    }

    @Override
    public boolean pauseQuartzTrigger(String schedulerName, String triggerName, String triggerGroup) {
        return setQuartzTriggerPaused(schedulerName, triggerName, triggerGroup, true);
    }

    @Override
    public boolean pauseQuartzJob(String schedulerName, String jobName, String jobGroup) {
        return setQuartzJobPaused(schedulerName, jobName, jobGroup, true);
    }

    @Override
    public boolean resumeQuartzTrigger(String schedulerName, String triggerName, String triggerGroup) {
        return setQuartzTriggerPaused(schedulerName, triggerName, triggerGroup, false);
    }

    @Override
    public boolean resumeQuartzJob(String schedulerName, String jobName, String jobGroup) {
        return setQuartzJobPaused(schedulerName, jobName, jobGroup, false);
    }

    private boolean setQuartzTriggerPaused(
            String schedulerName, String triggerName, String triggerGroup, boolean paused) {
        for (Scheduler scheduler : schedulers) {
            if (schedulerName.equals(getSchedulerName(scheduler))) {
                try {
                    TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
                    if (!scheduler.checkExists(triggerKey)) {
                        return false;
                    }
                    if (paused) {
                        scheduler.pauseTrigger(triggerKey);
                    } else {
                        scheduler.resumeTrigger(triggerKey);
                    }
                    return true;
                } catch (SchedulerException e) {
                    throw new QuartzAccessException("Could not update Quartz trigger pause state", e);
                }
            }
        }
        return false;
    }

    private ServiceQuartzJobs getSchedulerJobs(Scheduler scheduler) {
        try {
            List<QuartzJob> jobs = new ArrayList<>();
            for (JobKey key : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                JobDetail job = scheduler.getJobDetail(key);
                if (job != null) {
                    jobs.add(quartzAssembler.assembleJob(job, getQuartzTriggers(scheduler, job)));
                }
            }
            List<QuartzJobExecution> executions = new ArrayList<>();
            for (JobExecutionContext execution : scheduler.getCurrentlyExecutingJobs()) {
                executions.add(quartzAssembler.assembleExecution(execution));
            }
            return new ServiceQuartzJobs().jobs(jobs).executions(executions);
        } catch (SchedulerException e) {
            throw new QuartzAccessException("Could not retrieve Quartz jobs", e);
        }
    }

    private @Nullable String getSchedulerName(Scheduler scheduler) {
        try {
            return scheduler.getSchedulerName();
        } catch (SchedulerException e) {
            logger.warn("Could not retrieve Quartz scheduler name", e);
            return null;
        }
    }

    private List<QuartzTrigger> getQuartzTriggers(Scheduler scheduler, JobDetail job) throws SchedulerException {
        List<QuartzTrigger> triggers = new ArrayList<>();
        for (Trigger trigger : scheduler.getTriggersOfJob(job.getKey())) {
            Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
            if (state != Trigger.TriggerState.NONE) {
                triggers.add(quartzAssembler.assembleTrigger(trigger, state));
            }
        }
        return triggers;
    }

    private boolean setQuartzJobPaused(String schedulerName, String jobName, String jobGroup, boolean paused) {
        for (Scheduler scheduler : schedulers) {
            if (schedulerName.equals(getSchedulerName(scheduler))) {
                try {
                    JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
                    if (!scheduler.checkExists(jobKey)) {
                        return false;
                    }
                    if (paused) {
                        scheduler.pauseJob(jobKey);
                    } else {
                        scheduler.resumeJob(jobKey);
                    }
                    return true;
                } catch (SchedulerException e) {
                    throw new QuartzAccessException("Could not update Quartz job pause state", e);
                }
            }
        }
        return false;
    }
}
