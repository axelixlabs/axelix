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

import type { IKotlinReleaseUsage, ILanguageMix } from "@/models";
import { DONUT_COLORS, JAVA_ONLY_COLOR, KOTLIN_COLOR } from "@/utils";

import { LanguageProfileCard } from "../LanguageProfileCard";

interface IProps {
    applicationsTotal: number;
    languageMix: ILanguageMix;
    kotlinReleases: IKotlinReleaseUsage[];
    lastScan?: string;
}

export const KotlinCards = ({ applicationsTotal, languageMix, kotlinReleases, lastScan }: IProps) => {
    const { t } = useTranslation();
    const { kotlinApplications } = languageMix;

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
                eyebrow={t("Dashboard.Languages.languageMixEyebrow")}
                title={t("Dashboard.Languages.languageMixTitle")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Languages.kotlinOfApplications"
                        values={{ kotlin: kotlinApplications, total: applicationsTotal }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                segments={[
                    { value: languageMix.javaOnlyPercentage, color: JAVA_ONLY_COLOR },
                    { value: languageMix.kotlinPercentage, color: KOTLIN_COLOR },
                ]}
                centreTop={`${languageMix.kotlinPercentage}%`}
                centreBottom={t("Dashboard.Languages.useKotlin")}
                rows={[
                    {
                        color: JAVA_ONLY_COLOR,
                        label: t("Dashboard.Languages.javaOnly"),
                        cells: [`${languageMix.javaOnlyApplications}`, `${languageMix.javaOnlyPercentage}%`],
                    },
                    {
                        color: KOTLIN_COLOR,
                        label: t("Dashboard.Languages.javaAndKotlin"),
                        cells: [`${languageMix.kotlinApplications}`, `${languageMix.kotlinPercentage}%`],
                    },
                ]}
                footerLeft={detectedFooter(applicationsTotal)}
                footerRight={lastScan}
            />

            <LanguageProfileCard
                eyebrow={t("Dashboard.Languages.kotlinVersionEyebrow")}
                title={t("Dashboard.Languages.kotlinCardTitle")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Languages.versionsAcrossApplications"
                        values={{ count: kotlinReleases.length, apps: kotlinApplications }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                segments={kotlinReleases.map((release, index) => ({
                    value: release.applicationPercentage,
                    color: DONUT_COLORS[index % DONUT_COLORS.length],
                }))}
                centreTop={`${kotlinApplications}`}
                centreBottom={t("Dashboard.Languages.applicationsLower")}
                rows={kotlinReleases.map((release, index) => ({
                    color: DONUT_COLORS[index % DONUT_COLORS.length],
                    label: `${t("Dashboard.Languages.kotlinTitle")} ${release.release}`,
                    cells: [`${release.applicationCount}`, `${release.applicationPercentage}%`],
                }))}
                footerLeft={detectedFooter(kotlinApplications)}
                footerRight={lastScan}
            />
        </>
    );
};
