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
import type { JSX } from "react";
import { Trans, useTranslation } from "react-i18next";

import type { IJavaReleaseUsage, IJdkVendorUsage } from "@/models";
import { DONUT_COLORS } from "@/utils";

import { LanguageProfileCard } from "../LanguageProfileCard";

interface IProps {
    applicationsTotal: number;
    applicationsOnLts: number;
    javaReleases: IJavaReleaseUsage[];
    jdkVendors: IJdkVendorUsage[];
    lastScan?: string;
}

const percentageOf = (count: number, total: number) => (total === 0 ? 0 : Math.round((count * 100) / total));

const centreTop = (usage?: { applicationPercentage: number }) => (usage ? `${usage.applicationPercentage}%` : "—");

export const JavaCards = ({ applicationsTotal, applicationsOnLts, javaReleases, jdkVendors, lastScan }: IProps) => {
    const { t } = useTranslation();

    const newestJavaRelease = javaReleases[0];
    const topJdkVendor = jdkVendors[0];

    const detectedFooter = (count: number): JSX.Element => (
        <Trans
            t={t}
            i18nKey="Dashboard.Languages.detectedAcrossApplications"
            values={{ count }}
            components={[<b key="0" />]}
        />
    );

    return (
        <>
            <LanguageProfileCard
                eyebrow={t("Dashboard.Languages.javaEyebrow")}
                title={t("Dashboard.Languages.javaCardTitle")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Languages.releasesInUse"
                        values={{ count: javaReleases.length, apps: applicationsTotal }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                segments={javaReleases.map((release, index) => ({
                    value: release.applicationPercentage,
                    color: DONUT_COLORS[index % DONUT_COLORS.length],
                }))}
                centreTop={centreTop(newestJavaRelease)}
                centreBottom={
                    newestJavaRelease ? t("Dashboard.Languages.onJava", { release: newestJavaRelease.release }) : ""
                }
                summary={{
                    label: t("Dashboard.Languages.ltsReleases"),
                    value: `${applicationsOnLts} · ${percentageOf(applicationsOnLts, applicationsTotal)}%`,
                }}
                rows={javaReleases.map((release, index) => ({
                    color: DONUT_COLORS[index % DONUT_COLORS.length],
                    label: `${t("Dashboard.Languages.javaTitle")} ${release.release}`,
                    cells: [`${release.applicationCount}`, `${release.applicationPercentage}%`],
                }))}
                footerLeft={detectedFooter(applicationsTotal)}
                footerRight={lastScan}
            />

            <LanguageProfileCard
                eyebrow={t("Dashboard.Languages.jdkEyebrow")}
                title={t("Dashboard.Languages.jdkCardTitle")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Languages.vendorsAcrossApplications"
                        values={{ vendors: jdkVendors.length, apps: applicationsTotal }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                segments={jdkVendors.map((vendor, index) => ({
                    value: vendor.applicationPercentage,
                    color: DONUT_COLORS[index % DONUT_COLORS.length],
                }))}
                centreTop={centreTop(topJdkVendor)}
                centreBottom={t("Dashboard.Languages.singleJdk")}
                rows={jdkVendors.map((vendor, index) => ({
                    color: DONUT_COLORS[index % DONUT_COLORS.length],
                    label: vendor.vendor,
                    cells: [`${vendor.applicationCount}`, `${vendor.applicationPercentage}%`],
                }))}
                footerLeft={detectedFooter(applicationsTotal)}
                footerRight={lastScan}
            />
        </>
    );
};
