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
import { EThreadState } from "models";

export const threadDumpStateLetters: Record<EThreadState, string> = {
    [EThreadState.NEW]: "N",
    [EThreadState.RUNNABLE]: "R",
    [EThreadState.BLOCKED]: "B",
    [EThreadState.WAITING]: "W",
    [EThreadState.TIMED_WAITING]: "T",
    [EThreadState.TERMINATED]: "F",
};

/**
 * Constant that represents the length of the sliding window of the thread dump in milliseconds.
 *
 * The sliding window is the window that essentially answers the question - for how long do we
 * retain the previous thread dump snapshots.
 */
export const THREAD_DUMP_SLIDING_WINDOW_MS = 5 * 60 * 1000; // 5 min.

/**
 * The length of the single segment as it is displayed in the timeline.
 */
export const TIMELINE_SEGMENT_INTERVAL_MS = 15 * 1000;

/**
 * The interval between short-polling calls to the backend for the thread dump snapshot.
 */
export const THREAD_DUMP_SHORT_POLLING_INTERVAL_MS = 1000;
