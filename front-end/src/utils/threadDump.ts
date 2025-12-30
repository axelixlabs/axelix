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
import { EThreadState } from "models";

export const threadDumpStateLetters: Record<EThreadState, string> = {
    [EThreadState.NEW]: "N",
    [EThreadState.RUNNABLE]: "R",
    [EThreadState.BLOCKED]: "B",
    [EThreadState.WAITING]: "W",
    [EThreadState.TIMED_WAITING]: "T",
    [EThreadState.TERMINATED]: "F",
};

// 5 minutes in milliseconds
export const THREAD_DUMP_INTERVAL_MS = 5 * 60 * 1000;

// 15 seconds in milliseconds
export const TIMELINE_SEGMENT_INTERVAL_MS = 15 * 1000;
