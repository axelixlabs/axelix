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
export interface IJavaReleaseUsage {
    release: number;
    applicationCount: number;
    applicationPercentage: number;
    lts: boolean;
}

export interface IJdkVendorUsage {
    vendor: string;
    applicationCount: number;
    applicationPercentage: number;
}

export interface IKotlinReleaseUsage {
    release: string;
    applicationCount: number;
    applicationPercentage: number;
}

export interface ILanguageMix {
    javaOnlyApplications: number;
    javaOnlyPercentage: number;
    kotlinApplications: number;
    kotlinPercentage: number;
}

export interface ILanguagesProfileResponseBody {
    applicationsOnLts: number;
    applicationsOnNonLts: number;
    javaReleases: IJavaReleaseUsage[];
    jdkVendors: IJdkVendorUsage[];
    languageMix: ILanguageMix | null;
    kotlinReleases: IKotlinReleaseUsage[];
    lastScannedAt: string | null;
}

export interface ILanguagesLegendRow {
    color: string;
    label: string;
    cells: string[];
}

export interface ILanguagesDonutSegment {
    value: number;
    color: string;
}
