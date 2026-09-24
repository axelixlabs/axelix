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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler, Loader } from "@/components";
import { fetchData } from "@/helpers";
import { type IDashboardSpringPortfolioResponseBody, StatefulRequest } from "@/models";
import { getDashboardSpringPortfolioData } from "@/services";

import { MaintenanceLadder } from "./MaintenanceLadder";
import { PlatformDistributionCard } from "./PlatformDistributionCard";
import { PortfolioSummary } from "./PortfolioSummary";
import styles from "./styles.module.css";

const DashboardSpringPortfolio = () => {
    const { t } = useTranslation();
    const [dashboardSpringPortfolioData, setDashboardSpringPortfolioData] = useState(
        StatefulRequest.loading<IDashboardSpringPortfolioResponseBody>(),
    );

    useEffect(() => {
        fetchData(setDashboardSpringPortfolioData, () => getDashboardSpringPortfolioData());
    }, []);

    if (dashboardSpringPortfolioData.loading) {
        return <Loader />;
    }

    if (dashboardSpringPortfolioData.error) {
        return <EmptyHandler isEmpty />;
    }

    const portfolio = dashboardSpringPortfolioData.response!;

    return (
        <>
            <EmptyHandler isEmpty={portfolio.applicationsTotal === 0}>
                {/* TODO: Improve in future */}
                <div className={styles.Header}>
                    <div>
                        <div className="TextLarge">{t("Dashboard.SpringPortfolio.title")}</div>
                        <p className={styles.Subtitle}>{t("Dashboard.SpringPortfolio.subtitle")}</p>
                    </div>
                    <PortfolioSummary
                        applicationsTotal={portfolio.applicationsTotal}
                        applicationsFullyOssSupported={portfolio.applicationsFullyOssSupported}
                    />
                </div>

                <div className={styles.CardsWrapper}>
                    <PlatformDistributionCard distribution={portfolio.springBoot} />
                    <PlatformDistributionCard distribution={portfolio.springFramework} />
                </div>

                {portfolio.linesInUse.length > 0 && <MaintenanceLadder entries={portfolio.linesInUse} />}
            </EmptyHandler>
        </>
    );
};

export default DashboardSpringPortfolio;
