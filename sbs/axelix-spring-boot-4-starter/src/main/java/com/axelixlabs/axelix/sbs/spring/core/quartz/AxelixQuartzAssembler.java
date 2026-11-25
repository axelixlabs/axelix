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

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.quartz.CalendarIntervalTrigger;
import org.quartz.CronTrigger;
import org.quartz.DailyTimeIntervalTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobExecution;
import com.axelixlabs.axelix.common.contract.quartz.QuartzMisfirePolicy;
import com.axelixlabs.axelix.common.contract.quartz.QuartzSchedulerMetadata;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTrigger;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerType;

/**
 * Assembles Quartz scheduler, job and trigger information.
 *
 * @author Nikita Kirillov
 */
public class AxelixQuartzAssembler {

    public QuartzJob assembleJob(JobDetail job, List<QuartzTrigger> triggers) {
        return new QuartzJob()
                .name(job.getKey().getName())
                .group(job.getKey().getGroup())
                .className(job.getJobClass().getName())
                .description(job.getDescription())
                .concurrentExecutionDisallowed(job.isConcurrentExecutionDisallowed())
                .requestsRecovery(job.requestsRecovery())
                .durable(job.isDurable())
                .persistJobDataAfterExecution(job.isPersistJobDataAfterExecution())
                .triggers(triggers);
    }

    public QuartzJobExecution assembleExecution(JobExecutionContext execution) {
        return new QuartzJobExecution()
                .executionId(execution.getFireInstanceId())
                .jobName(execution.getJobDetail().getKey().getName())
                .jobGroup(execution.getJobDetail().getKey().getGroup())
                .triggerName(execution.getTrigger().getKey().getName())
                .triggerGroup(execution.getTrigger().getKey().getGroup())
                .startedAt(execution.getFireTime().toInstant().toString());
    }

    public QuartzTrigger assembleTrigger(Trigger trigger, Trigger.TriggerState state) {
        QuartzTrigger result = new QuartzTrigger()
                .name(trigger.getKey().getName())
                .group(trigger.getKey().getGroup())
                .type(QuartzTriggerType.CUSTOM)
                .state(state.name())
                .misfirePolicy(assembleMisfirePolicy(trigger))
                .previousFireTime(formatTimestamp(trigger.getPreviousFireTime()))
                .nextFireTime(formatTimestamp(trigger.getNextFireTime()));
        if (trigger instanceof CronTrigger cron) {
            result.type(QuartzTriggerType.CRON)
                    .cronExpression(cron.getCronExpression())
                    .timeZone(cron.getTimeZone().getID());
        } else if (trigger instanceof SimpleTrigger simple) {
            result.type(QuartzTriggerType.SIMPLE)
                    .interval(simple.getRepeatInterval())
                    .intervalUnit("MILLISECOND")
                    .repeatCount(simple.getRepeatCount());
        } else if (trigger instanceof CalendarIntervalTrigger calendar) {
            result.type(QuartzTriggerType.CALENDAR_INTERVAL)
                    .interval((long) calendar.getRepeatInterval())
                    .intervalUnit(calendar.getRepeatIntervalUnit().name())
                    .timeZone(calendar.getTimeZone().getID());
        } else if (trigger instanceof DailyTimeIntervalTrigger daily) {
            result.type(QuartzTriggerType.DAILY_TIME_INTERVAL)
                    .interval((long) daily.getRepeatInterval())
                    .intervalUnit(daily.getRepeatIntervalUnit().name())
                    .repeatCount(daily.getRepeatCount())
                    .daysOfWeek(daily.getDaysOfWeek().stream().sorted().toList())
                    .startTimeOfDay(formatTimeOfDay(daily.getStartTimeOfDay()))
                    .endTimeOfDay(formatEndTimeOfDay(daily.getEndTimeOfDay()));
        }
        return result;
    }

    public QuartzSchedulerMetadata assembleSchedulerMetadata(SchedulerMetaData metadata) {
        return new QuartzSchedulerMetadata()
                .schedulerName(metadata.getSchedulerName())
                .schedulerInstanceId(metadata.getSchedulerInstanceId())
                .version(metadata.getVersion())
                .schedulerClassName(metadata.getSchedulerClass().getName())
                .schedulerRemote(metadata.isSchedulerRemote())
                .started(metadata.isStarted())
                .inStandbyMode(metadata.isInStandbyMode())
                .shutdown(metadata.isShutdown())
                .startedAt(formatTimestamp(metadata.getRunningSince()))
                .numberOfJobsExecuted(metadata.getNumberOfJobsExecuted())
                .jobStoreClassName(metadata.getJobStoreClass().getName())
                .jobStoreSupportsPersistence(metadata.isJobStoreSupportsPersistence())
                .jobStoreClustered(metadata.isJobStoreClustered())
                .threadPoolClassName(metadata.getThreadPoolClass().getName())
                .threadPoolSize(metadata.getThreadPoolSize());
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private QuartzMisfirePolicy assembleMisfirePolicy(Trigger trigger) {
        int instruction = trigger.getMisfireInstruction();
        if (instruction == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
            return QuartzMisfirePolicy.IGNORE_MISFIRES;
        }
        if (instruction == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY) {
            return QuartzMisfirePolicy.SMART_POLICY;
        }
        if (trigger instanceof SimpleTrigger) {
            switch (instruction) {
                case SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW:
                    return QuartzMisfirePolicy.FIRE_NOW;
                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT:
                    return QuartzMisfirePolicy.RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT;
                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT:
                    return QuartzMisfirePolicy.RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT;
                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT:
                    return QuartzMisfirePolicy.RESCHEDULE_NEXT_WITH_EXISTING_COUNT;
                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT:
                    return QuartzMisfirePolicy.RESCHEDULE_NEXT_WITH_REMAINING_COUNT;
                default:
                    return QuartzMisfirePolicy.UNKNOWN;
            }
        }
        if (trigger instanceof CronTrigger
                || trigger instanceof CalendarIntervalTrigger
                || trigger instanceof DailyTimeIntervalTrigger) {
            switch (instruction) {
                case CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:
                    return QuartzMisfirePolicy.FIRE_ONCE_NOW;
                case CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:
                    return QuartzMisfirePolicy.DO_NOTHING;
                default:
                    return QuartzMisfirePolicy.UNKNOWN;
            }
        }
        return QuartzMisfirePolicy.UNKNOWN;
    }

    /**
     * Uses the same default end time as
     * {@link DailyTimeIntervalTriggerImpl#getFireTimeAfter(Date)}.
     */
    private String formatEndTimeOfDay(@Nullable TimeOfDay time) {
        return time == null ? "23:59:59" : formatTimeOfDay(time);
    }

    private String formatTimeOfDay(TimeOfDay time) {
        return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond()).toString();
    }

    private @Nullable String formatTimestamp(@Nullable Date timestamp) {
        return timestamp != null ? timestamp.toInstant().toString() : null;
    }
}
