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
import { EThreadDumpStateColors, EThreadState, type IThread } from "models";
import { FIFTEEN_SECONDS, TEN_MINUTES_MILLISECDONDS } from "utils";

export const getThreadStateColor = (threadDump: IThread) => {
    const { threadState, inNative, suspended } = threadDump;

    if (threadState === EThreadState.RUNNABLE) {
        return inNative ? EThreadDumpStateColors.BLUE : EThreadDumpStateColors.GREEN;
    }

    if ((threadState === EThreadState.WAITING && !suspended) || threadState === EThreadState.TIMED_WAITING) {
        return EThreadDumpStateColors.ORANGE;
    }

    if (threadState === EThreadState.WAITING) {
        return EThreadDumpStateColors.YELLOW;
    }

    if (threadState === EThreadState.BLOCKED) {
        return EThreadDumpStateColors.RED;
    }

    if (threadState === EThreadState.NEW) {
        return EThreadDumpStateColors.WHITE;
    }

    if (threadState === EThreadState.TERMINATED) {
        return EThreadDumpStateColors.GREY;
    }
};

export const generateTimeSlots = () => {
    const now = new Date();

    // 15 seconds interval between time slots
    const stepMilliseconds = FIFTEEN_SECONDS;

    // 10 minutes ahead from now
    const endTime = new Date(now.getTime() + TEN_MINUTES_MILLISECDONDS);
    const slots: Date[] = [];

    let currentTime = new Date(now);

    // generate all time slots from current time to endTime with a 15-second interval
    while (currentTime <= endTime) {
        slots.push(new Date(currentTime));
        currentTime = new Date(currentTime.getTime() + stepMilliseconds);
    }

    return slots;
};
