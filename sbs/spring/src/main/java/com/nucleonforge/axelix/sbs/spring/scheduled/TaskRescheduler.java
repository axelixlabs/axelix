/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.scheduled;

import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.config.ScheduledTask;

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
     * Re-schedule the provided task.
     * @param task task to re-schedule.
     */
    void reschedule(ManagedScheduledTask task);

    /**
     * Checks whether the given scheduled task is compatible with this handler.
     * @param task task for evaluation.
     * @return {@code true} if the current {@link TaskRescheduler} supports the provided task.
     */
    boolean supports(ManagedScheduledTask task);

    /**
     * Mutate the configuration of the given scheduled task.
     *
     * @param task task to re-schedule.
     * @param newValue the new value to apply to the task's configuration.
     */
    void mutate(ManagedScheduledTask task, String newValue);
}
