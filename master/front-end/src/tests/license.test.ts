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
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { getLicenseDaysLeft, isLicenseExpiringSoon, shouldShowLicenseKeyAlert } from "@/helpers";
import { LICENSE_ALERT_DISMISSED_AT_KEY, MS_IN_DAY } from "@/utils";

const NOW = new Date(2026, 0, 15, 10, 0, 0, 0);

describe("getLicenseDaysLeft", () => {
    it("Rounds partial days up", () => {
        expect(getLicenseDaysLeft(MS_IN_DAY)).toBe(1);
        expect(getLicenseDaysLeft(MS_IN_DAY + 1)).toBe(2);
        expect(getLicenseDaysLeft(MS_IN_DAY / 2)).toBe(1);
        expect(getLicenseDaysLeft(0)).toBe(0);
    });
});

describe("shouldShowLicenseKeyAlert", () => {
    let store: Record<string, string> = {};

    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(NOW);

        store = {};
        Object.defineProperty(globalThis, "localStorage", {
            configurable: true,
            value: {
                getItem: (key: string): string | null => (key in store ? store[key] : null),
                setItem: (key: string, value: string): void => {
                    store[key] = value;
                },
            },
        });
    });

    afterEach(() => {
        vi.useRealTimers();
        Object.defineProperty(globalThis, "localStorage", { configurable: true, value: undefined });
    });

    it("Shows the alert when it was never dismissed", () => {
        expect(shouldShowLicenseKeyAlert()).toBe(true);
    });

    it("Hides the alert when dismissed less than 24 hours ago", () => {
        store[LICENSE_ALERT_DISMISSED_AT_KEY] = String(NOW.getTime() - (MS_IN_DAY - 1));

        expect(shouldShowLicenseKeyAlert()).toBe(false);
    });

    it("Shows the alert again once 24 hours have elapsed since dismissal", () => {
        store[LICENSE_ALERT_DISMISSED_AT_KEY] = String(NOW.getTime() - MS_IN_DAY);

        expect(shouldShowLicenseKeyAlert()).toBe(true);
    });

    it("Shows the alert for a future dismissal timestamp", () => {
        store[LICENSE_ALERT_DISMISSED_AT_KEY] = String(NOW.getTime() + MS_IN_DAY);

        expect(shouldShowLicenseKeyAlert()).toBe(true);
    });
});

describe("isLicenseExpiringSoon", () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(NOW);
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    const inDays = (days: number): string => new Date(NOW.getTime() + days * MS_IN_DAY).toISOString();

    it("Returns false when there is no expiry date", () => {
        expect(isLicenseExpiringSoon(null)).toBe(false);
    });

    it("Returns false for an already expired license", () => {
        expect(isLicenseExpiringSoon(inDays(-1))).toBe(false);
    });

    it("Returns true within the expiring-soon threshold", () => {
        expect(isLicenseExpiringSoon(inDays(14))).toBe(true);
    });

    it("Returns false beyond the expiring-soon threshold", () => {
        expect(isLicenseExpiringSoon(inDays(15))).toBe(false);
    });
});
