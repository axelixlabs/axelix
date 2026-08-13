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
import dayjs from "dayjs";
import type { TFunction } from "i18next";

import type { ILicensing } from "models";
import { IS_ENTERPRISE_FLAG } from "utils";

export const isEnterpriseLicense = (licensing: ILicensing): boolean => licensing.license === IS_ENTERPRISE_FLAG;

export const getTimeLeftText = (validUntil: string | null, t: TFunction) => {
    if (!validUntil) {
        return t("LicenseModal.timeLeft.unknown");
    }

    const now = dayjs();
    const validUntilTarget = dayjs(validUntil);

    const daysLeft = validUntilTarget.diff(now, "day");

    if (daysLeft < 0) {
        return t("LicenseModal.timeLeft.expired");
    }

    if (daysLeft < 10) {
        const totalMinutes = validUntilTarget.diff(now, "minute");
        const days = Math.floor(totalMinutes / (60 * 24));
        const hours = Math.floor((totalMinutes % (60 * 24)) / 60);
        const minutes = totalMinutes % 60;

        return t("LicenseModal.timeLeft.detailed", { days: days, hours: hours, minutes: minutes });
    }

    return t("LicenseModal.timeLeft.days", { count: daysLeft });
};
