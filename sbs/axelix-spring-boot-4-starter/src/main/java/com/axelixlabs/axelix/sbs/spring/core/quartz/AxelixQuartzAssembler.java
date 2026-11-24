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
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
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
                .triggers(triggers);
    }

    public QuartzTrigger assembleTrigger(Trigger trigger, Trigger.TriggerState state) {
        QuartzTrigger result = new QuartzTrigger()
                .name(trigger.getKey().getName())
                .group(trigger.getKey().getGroup())
                .type(QuartzTriggerType.CUSTOM)
                .state(state.name())
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
                    .endTimeOfDay(formatTimeOfDay(daily.getEndTimeOfDay()));
        }
        return result;
    }

    private String formatTimeOfDay(TimeOfDay time) {
        return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond()).toString();
    }

    private @Nullable String formatTimestamp(@Nullable Date timestamp) {
        return timestamp != null ? timestamp.toInstant().toString() : null;
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
}
