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
import { Pagination } from "antd";
import dayjs from "dayjs";
import type { TFunction } from "i18next";
import { useLayoutEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

import type { IObservedService } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    services: IObservedService[];
}

const formatLastSeen = (lastSeen: string, t: TFunction): string => {
    const seen = dayjs(lastSeen);

    if (!seen.isValid()) {
        return lastSeen;
    }

    const daysAgo = dayjs().startOf("day").diff(seen.startOf("day"), "day");

    if (daysAgo <= 0) {
        return t("Upgrades.ArtifactsTable.today");
    }

    if (daysAgo === 1) {
        return t("Upgrades.ArtifactsTable.yesterday");
    }

    return t("Upgrades.ArtifactsTable.daysAgo", { count: daysAgo });
};

const BOTTOM_SPACING = 24;

export const UpgradesTable = ({ services }: IProps) => {
    const { t } = useTranslation();
    const [firstVisibleIndex, setFirstVisibleIndex] = useState(0);
    const [pageSize, setPageSize] = useState(1);
    const rowsRef = useRef<HTMLDivElement>(null);
    const footerRef = useRef<HTMLDivElement>(null);
    const total = services.length;
    const page = Math.min(Math.floor(firstVisibleIndex / pageSize) + 1, Math.ceil(total / pageSize));
    const firstIndex = (page - 1) * pageSize;
    const visibleServices = services.slice(firstIndex, firstIndex + pageSize);

    useLayoutEffect(() => {
        const rows = rowsRef.current!;
        const footer = footerRef.current!;
        const section = rows.closest("section")!;
        const updatePageSize = (): void => {
            const rowHeight = rows.firstElementChild?.getBoundingClientRect().height;

            if (!rowHeight) {
                return;
            }

            // Measure in document coordinates so scrolling does not change page boundaries.
            const availableHeight =
                window.innerHeight -
                (rows.getBoundingClientRect().top + window.scrollY) -
                footer.getBoundingClientRect().height -
                BOTTOM_SPACING -
                1;

            const nextPageSize = Math.max(1, Math.floor(availableHeight / rowHeight));
            setPageSize(nextPageSize);
        };

        const observer = new ResizeObserver(updatePageSize);
        observer.observe(section.parentElement!);
        observer.observe(footer);
        window.addEventListener("resize", updatePageSize);
        updatePageSize();

        return () => {
            observer.disconnect();
            window.removeEventListener("resize", updatePageSize);
        };
    }, [total]);

    return (
        <section className={styles.Section}>
            <div className={styles.SectionRow}>
                <h2 className={styles.SectionTitle} id="observed-services">
                    {t("Upgrades.ArtifactsTable.sectionTitle")}
                </h2>
                <p className={styles.SectionNote}>{t("Upgrades.FleetBars.serviceCount", { count: total })}</p>
            </div>

            <div className={styles.Table}>
                <div className={styles.Columns} role="table" aria-labelledby="observed-services">
                    <div role="row" className={`TextCaps ${styles.HeaderRow}`}>
                        <span role="columnheader">{t("Upgrades.ArtifactsTable.artifact")}</span>
                        <span role="columnheader">{t("Upgrades.ArtifactsTable.starter")}</span>
                        <span role="columnheader">{t("Upgrades.ArtifactsTable.lastSeen")}</span>
                    </div>

                    <div className={styles.Rows} role="rowgroup" ref={rowsRef}>
                        {visibleServices.map(({ artifactId, groupId, starterVersion, lastSeen }) => (
                            <div role="row" className={styles.Row} key={`${groupId}:${artifactId}`}>
                                <div role="cell" className={styles.ArtifactCell}>
                                    <span className={styles.ArtifactId} title={artifactId}>
                                        {artifactId}
                                    </span>
                                    <span className={styles.GroupId} title={groupId}>
                                        {groupId}
                                    </span>
                                </div>
                                <span role="cell" className={styles.VersionBadge} title={starterVersion}>
                                    {starterVersion}
                                </span>
                                <span role="cell" className={styles.LastSeen}>
                                    {formatLastSeen(lastSeen, t)}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>

                <div className={`TextUltraSmall ${styles.Footer}`} ref={footerRef}>
                    <span aria-live="polite">
                        {t("Upgrades.ArtifactsTable.range", {
                            from: firstIndex + 1,
                            to: firstIndex + visibleServices.length,
                            total,
                        })}
                    </span>
                    <Pagination
                        size="small"
                        current={page}
                        pageSize={pageSize}
                        total={total}
                        hideOnSinglePage
                        showSizeChanger={false}
                        onChange={(nextPage) => setFirstVisibleIndex((nextPage - 1) * pageSize)}
                    />
                </div>
            </div>
        </section>
    );
};
