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
import { Button } from "antd";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

import { DOCS_URL, ISO_DATE_FORMAT } from "@/utils";

import { DashboardNoDataSkeleton } from "./DashboardNoDataSkeleton";
import styles from "./styles.module.css";

export const DashboardNoData = () => {
    const { t } = useTranslation();

    const lastScan = dayjs().format(ISO_DATE_FORMAT);

    return (
        <>
            <div className={styles.MainWrapper} data-dashboard-no-data>
                <div className={styles.SkeletonBackground}>
                    <DashboardNoDataSkeleton />
                </div>
                <div className={styles.Overlay} />
                <div className={styles.CardContainer}>
                    <div className={styles.Card}>
                        <div className={`TextSmall ${styles.Badge}`}>
                            <div className={styles.BadgeDot} />
                            {t("Dashboard.NoData.badge")}
                        </div>

                        <div className={`TextMedium ${styles.Title}`}>{t("Dashboard.NoData.title")}</div>

                        <div className="TextSmall">{t("Dashboard.NoData.noApplications")}</div>
                        <div className="TextSmall">{t("Dashboard.NoData.oldStarter")}</div>

                        <div className={styles.Footer}>
                            <Button type="primary" href={DOCS_URL} target="_blank" rel="noopener noreferrer">
                                {t("Dashboard.NoData.readTheDocs")}
                            </Button>

                            <div className={`TextSmall ${styles.LastScan}`}>
                                {t("Dashboard.NoData.lastScan", { date: lastScan })}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};
