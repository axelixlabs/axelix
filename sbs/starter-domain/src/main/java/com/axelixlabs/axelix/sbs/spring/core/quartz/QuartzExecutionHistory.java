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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.contract.metadata.QuartzJobExecutionHistory;
import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;

/**
 * Bounded in-memory history of the latest Quartz job executions, kept per scheduler and job.
 *
 * @author Nikita Kirillov
 */
public class QuartzExecutionHistory {

    /**
     * The maximum number of the latest executions kept per job and reported to master.
     */
    private static final int HISTORY_MAX_SIZE = 10;

    private final Map<String, Map<JobIdentity, SlidingWindow<QuartzJobExecutionHistory>>> history =
            new ConcurrentHashMap<>();

    public void record(String schedulerName, QuartzJobExecutionHistory execution) {
        JobIdentity jobIdentity = new JobIdentity(execution.getJobName(), execution.getJobGroup());
        history.computeIfAbsent(schedulerName, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(jobIdentity, ignored -> new SlidingWindow<>(HISTORY_MAX_SIZE))
                .put(execution);
    }

    /**
     * @return the latest completed executions observed for the given scheduler.
     */
    public List<QuartzJobExecutionHistory> getExecutions(String schedulerName) {
        return history.getOrDefault(schedulerName, Map.of()).values().stream()
                .flatMap(window -> window.get().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @return the latest completed executions observed by all schedulers.
     */
    public List<QuartzJobExecutionHistory> getExecutions() {
        return history.values().stream()
                .flatMap(schedulerHistory -> schedulerHistory.values().stream())
                .flatMap(window -> window.get().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    private static final class JobIdentity {

        private final String name;
        private final String group;

        private JobIdentity(String name, String group) {
            this.name = name;
            this.group = group;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof JobIdentity)) {
                return false;
            }
            JobIdentity that = (JobIdentity) other;
            return name.equals(that.name) && group.equals(that.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, group);
        }
    }
}
