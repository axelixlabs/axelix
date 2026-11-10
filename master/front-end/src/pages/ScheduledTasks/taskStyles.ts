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
import successRateStyles from "@/pages/ScheduledTasks/successRate.module.css";

export const HEALTHY_SUCCESS_RATE = 90;

export const getSuccessRateClass = (successRate: number | null, enabled: boolean): string => {
    if (!enabled || successRate === null) {
        return successRateStyles.RateOff;
    }

    if (successRate === 100) {
        return successRateStyles.RateGood;
    }

    return successRate >= HEALTHY_SUCCESS_RATE ? successRateStyles.RateWarn : successRateStyles.RateBad;
};
