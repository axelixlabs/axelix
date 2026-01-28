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
package com.axelixlabs.axelix.sbs.spring.scheduled;

import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;

/**
 * Interface that is capable to "re-schedule" the {@link ManagedScheduledTask}. Understanding the
 * term "re-scheduling" here is very important, read extended javadoc for this.
 * <p>
 * The need for this abstraction emerges from the fact that {@link ScheduledFuture}, and
 * corresponding Spring's {@link ScheduledTask ScheduledTask handle} cannot be re-scheduled
 * in-place, that is to say that we cannot just {@link ScheduledTask#cancel()} or {@link ScheduledFuture#cancel(boolean)}
 * and then start the exactly same {@link ScheduledTask} or {@link ScheduledFuture} instance again.
 * That is just not possible by design.
 * <p>
 * Thus, in order to emulate the "re-scheduling" we have to follow the cancel-and-create pattern:
 * <ol>
 *     <li>Cancel the current {@link ScheduledTask}</li>
 *     <li>Submit a new {@link ScheduledTask} that is copy of the old one</li>
 *     <li>Clean-up the possible state left about the old task in the spring context</li>
 * </ol>
 * So, the above combination is what is actually meant by "re-scheduling".
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public interface TaskRescheduler {

    /**
     * Re-schedules the provided Axelix's {@link ManagedScheduledTask}, in accordance to the rules provided by the new provided
     * Spring's {@link Task}.
     *
     * @param task    task to re-schedule.
     * @param newTask new task schedule to be applied
     */
    void reschedule(ManagedScheduledTask task, Task newTask);

    /**
     * Re-schedules the {@link ManagedScheduledTask} without any changes in its schedule.
     *
     * @param task task to re-schedule.
     */
    default void reschedule(ManagedScheduledTask task) {
        this.reschedule(task, task.getTask());
    }

    /**
     * Checks whether the given scheduled task is compatible with this handler.
     * @param task task for evaluation.
     * @return {@code true} if the current {@link TaskRescheduler} supports the provided task.
     */
    boolean supports(ManagedScheduledTask task);
}
