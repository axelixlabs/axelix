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
import { type TFunction } from "i18next";
import { useEffect, useState } from "react";
import { Trans, useTranslation } from "react-i18next";

import { DashboardPagesFirstSection, EmptyHandler, Loader } from "@/components";
import { fetchData } from "@/helpers";
import { type ILanguagesProfileResponseBody, StatefulRequest } from "@/models";
import { getLanguagesProfileData } from "@/services";
import { DONUT_COLORS } from "@/utils";

import { KotlinCards } from "./KotlinCards";
import { LanguageProfileCard } from "./LanguageProfileCard";
import { LanguagesSummaryCard } from "./LanguagesSummaryCard";
import { NoKotlinBanner } from "./NoKotlinBanner";
import styles from "./styles.module.css";

const detectedFooter = (t: TFunction, count: number) => (
    <Trans
        t={t}
        i18nKey="Dashboard.Languages.detectedAcrossApplications"
        values={{ count }}
        components={[<b key="0" />]}
    />
);

const percentageOf = (count: number, total: number) => (total === 0 ? 0 : Math.round((count * 100) / total));

const centreTop = (usage?: { applicationPercentage: number }) => (usage ? `${usage.applicationPercentage}%` : "—");

const formatLastScan = (t: TFunction, lastScannedAt: string | null) => {
    if (!lastScannedAt) {
        return undefined;
    }

    const date = new Date(lastScannedAt).toLocaleDateString(undefined, {
        day: "numeric",
        month: "short",
        year: "numeric",
    });

    return t("Dashboard.Languages.lastScan", { date });
};

const DashboardLanguages = () => {
    const { t } = useTranslation();
    const [languagesProfile, setLanguagesProfile] = useState(StatefulRequest.loading<ILanguagesProfileResponseBody>());

    useEffect(() => {
        fetchData(setLanguagesProfile, () => getLanguagesProfileData());
    }, []);

    if (languagesProfile.loading) {
        return <Loader />;
    }

    if (languagesProfile.error) {
        return <EmptyHandler isEmpty />;
    }

    const {
        applicationsOnLts,
        applicationsOnNonLts,
        javaReleases,
        jdkVendors,
        languageMix,
        kotlinReleases,
        lastScannedAt,
    } = languagesProfile.response!;

    const applicationsTotal = applicationsOnLts + applicationsOnNonLts;
    const kotlinDetected = kotlinReleases.length > 0;

    const lastScan = formatLastScan(t, lastScannedAt);
    const newestJavaRelease = javaReleases[0];
    const topJdkVendor = jdkVendors[0];

    return (
        <>
            <div className={styles.FirstSectionWrapper}>
                <DashboardPagesFirstSection
                    title={t("Dashboard.Languages.title")}
                    subtitle={t("Dashboard.Languages.subtitle")}
                />
                <LanguagesSummaryCard
                    applicationsTotal={applicationsTotal}
                    applicationsOnLts={applicationsOnLts}
                    applicationsOnNonLts={applicationsOnNonLts}
                />
            </div>

            <div className={styles.CardsWrapper}>
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
                    footerLeft={detectedFooter(t, applicationsTotal)}
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
                    footerLeft={detectedFooter(t, applicationsTotal)}
                    footerRight={lastScan}
                />

                {kotlinDetected && languageMix ? (
                    <KotlinCards
                        applicationsTotal={applicationsTotal}
                        languageMix={languageMix}
                        kotlinReleases={kotlinReleases}
                        lastScan={lastScan}
                    />
                ) : (
                    <div className={styles.NoKotlinBannerWrapper}>
                        <NoKotlinBanner applicationsTotal={applicationsTotal} footerRight={lastScan} />
                    </div>
                )}
            </div>
        </>
    );
};

export default DashboardLanguages;
