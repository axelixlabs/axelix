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
import type { ETriggerType, IScheduledTask } from "./taskModel";

type ClassMap = { [key: string]: string };

/**
 * Picks the trigger-type badge class from the shared stylesheet.
 */
export const triggerTypeClass = (shared: ClassMap, type: ETriggerType): string => {
    if (type === "Cron") {
        return shared.TypeCron;
    }

    return type === "Fixed delay" ? shared.TypeFixedDelay : shared.TypeFixedRate;
};

/**
 * Picks the success-rate colour class: dim when the task is off or has no history, otherwise
 * green / amber / red by how healthy the rate is.
 */
export const successRateClass = (shared: ClassMap, task: IScheduledTask): string => {
    if (!task.enabled || !task.hasHistory) {
        return shared.RateOff;
    }

    if (task.successRate === 100) {
        return shared.RateGood;
    }

    return task.successRate >= 90 ? shared.RateWarn : shared.RateBad;
};
