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
import type { ICron, IFixedTasks } from "../interfaces/scheduledTasks";

/**
 * The trigger type, named after the response group it comes from.
 */
export type TScheduledTaskType = "cron" | "fixedDelay" | "fixedRate";

/**
 * A scheduled task trigger defined by an interval.
 */
export type TFixedScheduledTaskType = Exclude<TScheduledTaskType, "cron">;

/**
 * A scheduled task together with its trigger type.
 */
export type TScheduledTaskItem = { type: "cron"; task: ICron } | { type: TFixedScheduledTaskType; task: IFixedTasks };

export type TScheduledTaskExecutionsFilter = "all" | "failed";

export type TScheduledTaskStatus = "enable" | "disable";
