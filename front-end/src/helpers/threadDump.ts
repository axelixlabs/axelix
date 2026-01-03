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
import accordionStyles from "components/Accordion/styles.module.css";
import type { MouseEvent } from "react";

import { EThreadState, type IColorPallete, type IThread, type IThreadGroup } from "models";
import { THREAD_DUMP_SLIDING_WINDOW_MS, TIMELINE_SEGMENT_INTERVAL_MS, colorPalette } from "utils";

export const getThreadStateColor = (threadDump: IThread): IColorPallete => {
    const { threadState, inNative, suspended } = threadDump;

    if (threadState === EThreadState.RUNNABLE) {
        return inNative ? colorPalette.BLUE : colorPalette.GREEN;
    }

    if ((threadState === EThreadState.WAITING && !suspended) || threadState === EThreadState.TIMED_WAITING) {
        return colorPalette.ORANGE;
    }

    if (threadState === EThreadState.WAITING) {
        return colorPalette.YELLOW;
    }

    if (threadState === EThreadState.BLOCKED) {
        return colorPalette.RED;
    }

    if (threadState === EThreadState.NEW) {
        return colorPalette.WHITE;
    }

    if (threadState === EThreadState.TERMINATED) {
        return colorPalette.GREY;
    }

    return colorPalette.PURPLE;
};

export const generateTimeSlots = (): Date[] => {
    const now = new Date();

    // 15 seconds interval between time slots
    const stepMilliseconds = TIMELINE_SEGMENT_INTERVAL_MS;

    // 5 minutes ahead from now
    const endTime = new Date(now.getTime() + THREAD_DUMP_SLIDING_WINDOW_MS);
    const slots: Date[] = [];

    let currentTime = new Date(now);

    // generate all time slots from current time to endTime with a 15-second interval
    while (currentTime <= endTime) {
        slots.push(new Date(currentTime));
        currentTime = new Date(currentTime.getTime() + stepMilliseconds);
    }

    return slots;
};

const isSameThreadDumpGroup = (currentThreadGroup: IThreadGroup, thread: IThread): boolean => {
    const sameState = currentThreadGroup.thread.threadState === thread.threadState;
    const sameBlockedCount = currentThreadGroup.thread.blockedCount === thread.blockedCount;
    const sameWaitedCount = currentThreadGroup.thread.waitedCount === thread.waitedCount;

    return sameState && sameBlockedCount && sameWaitedCount;
};

export const partitionToThreadGroups = (history: IThread[]): IThreadGroup[] => {
    const threadGroups: IThreadGroup[] = [];
    let currentThreadGroup: IThreadGroup | null = null;

    history.forEach((thread, index) => {
        if (currentThreadGroup && isSameThreadDumpGroup(currentThreadGroup, thread)) {
            currentThreadGroup.count++;
            currentThreadGroup.thread = thread;
        } else {
            const id = `${thread.threadId}-${thread.threadState}-${thread.blockedCount}-${thread.waitedCount}-${index}`;

            currentThreadGroup = {
                id: id,
                thread: thread,
                count: 1,
            };

            threadGroups.push(currentThreadGroup);
        }
    });

    return threadGroups;
};

export const getDisplayedThreadDump = (thread: IThread, selectedGroups: Record<string, IThreadGroup>): IThread => {
    const threadGroup = selectedGroups[String(thread.threadId)];

    if (threadGroup) {
        return threadGroup.thread;
    }

    return thread;
};

export const stopPropagationOnAccordionExpand = (e: MouseEvent<HTMLDivElement>): void => {
    const timelineMainWrapper = e.currentTarget;
    const accordionWrapper = timelineMainWrapper.closest(`.${accordionStyles.MainWrapper}`) as HTMLElement | null;
    const contentVisible = accordionWrapper?.classList.contains(accordionStyles.Open) ?? false;

    if (contentVisible) {
        e.stopPropagation();
    }
};
