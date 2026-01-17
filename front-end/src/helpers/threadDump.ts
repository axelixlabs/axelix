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

export const filterThreadDump = (threadDump: IThread[], search: string): IThread[] => {
    const formattedSearch = search.toLowerCase().trim();

    return threadDump.filter(({ threadName }) => {
        const lowerThreadName = threadName.toLowerCase();
        return lowerThreadName.includes(formattedSearch);
    });
};

export const sortThreadDumpByPriority = (effectiveThreadDump: IThread[]): IThread[] => {
    return effectiveThreadDump.toSorted((currentThread, nextThread) => nextThread.priority - currentThread.priority);
};

export const appendToThreadDumpHistory = (
    previousState: Record<string, IThread[]>,
    sortedThreadDump: IThread[],
): Record<string, IThread[]> => {
    const actualState = { ...previousState };

    sortedThreadDump.forEach((thread) => {
        const { threadId } = thread;

        actualState[threadId] = (actualState[threadId] || []).concat(thread);
    });

    return actualState;
};
