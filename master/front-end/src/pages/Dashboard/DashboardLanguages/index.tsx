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
import { useTranslation } from "react-i18next";

import { DashboardNoData, DashboardPagesFirstSection, EmptyHandler, Loader } from "@/components";
import { fetchData } from "@/helpers";
import { type ILanguagesProfileResponseBody, StatefulRequest } from "@/models";
import { getLanguagesProfileData } from "@/services";

import { JavaCards } from "./JavaCards";
import { KotlinCards } from "./KotlinCards";
import { LanguagesSummaryCard } from "./LanguagesSummaryCard";
import { NoKotlinBanner } from "./NoKotlinBanner";
import styles from "./styles.module.css";

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

    if (applicationsTotal === 0) {
        return (
            <>
                <DashboardPagesFirstSection
                    title={t("Dashboard.Languages.title")}
                    subtitle={t("Dashboard.Languages.subtitle")}
                />

                <DashboardNoData />
            </>
        );
    }

    const kotlinDetected = kotlinReleases.length > 0;

    const lastScan = formatLastScan(t, lastScannedAt);

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
                <JavaCards
                    applicationsTotal={applicationsTotal}
                    applicationsOnLts={applicationsOnLts}
                    javaReleases={javaReleases}
                    jdkVendors={jdkVendors}
                    lastScan={lastScan}
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
