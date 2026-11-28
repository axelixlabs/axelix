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
import { FilterOutlined } from "@ant-design/icons";

import { Button, Popover, Tag } from "antd";
import { type MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";

import {
    formatSpringSliceRange,
    getSpringSliceVersions,
    isSpringSliceFullRange,
    normalizeSpringSlice,
    platformFullName,
} from "@/helpers";
import { EFrameworkSupportStatus, type ISpringPortfolioApplication, type ISpringSlice } from "@/models";

import { SpringSliceForm } from "./SpringSliceForm";
import styles from "./styles.module.css";

interface IProps {
    applications: ISpringPortfolioApplication[];
    slice: ISpringSlice | null;
    onChange: (slice: ISpringSlice | null) => void;
}

export const SpringSliceFilter = ({ applications, slice, onChange }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);

    const changeSlice = (nextSlice: ISpringSlice | null): void => {
        onChange(nextSlice && normalizeSpringSlice(applications, nextSlice));
        setOpen(false);
    };

    const hasRange = !!slice && !isSpringSliceFullRange(applications, slice);
    const hasStatus = !!slice && slice.status !== "ALL";
    const conditionsCount = Number(hasRange) + Number(hasStatus);

    const clearRange = (event: MouseEvent<HTMLElement>): void => {
        event.preventDefault();

        if (slice) {
            const versions = getSpringSliceVersions(applications, slice.platform);
            onChange(
                normalizeSpringSlice(applications, { ...slice, from: versions[0], to: versions[versions.length - 1] }),
            );
        }
    };

    const clearStatus = (event: MouseEvent<HTMLElement>): void => {
        event.preventDefault();

        if (slice) {
            onChange(normalizeSpringSlice(applications, { ...slice, status: "ALL" }));
        }
    };

    return (
        <>
            <Popover
                trigger="click"
                placement="bottomLeft"
                autoAdjustOverflow={false}
                arrow={false}
                open={open}
                onOpenChange={setOpen}
                destroyOnHidden
                align={{ offset: [0, 10] }}
                styles={{
                    root: { paddingBottom: "var(--axelix-space-6)" },
                    container: { padding: 0, borderRadius: "var(--axelix-border-radius-large)", overflow: "hidden" },
                }}
                content={
                    <SpringSliceForm
                        applications={applications}
                        slice={slice}
                        onApply={changeSlice}
                        onClear={() => changeSlice(null)}
                        onClose={() => setOpen(false)}
                    />
                }
            >
                <Button
                    className={`${styles.Trigger} ${conditionsCount ? styles.Filtered : ""}`}
                    icon={<FilterOutlined />}
                    aria-expanded={open}
                >
                    {t("Dashboard.SpringPortfolio.springSlice")}
                    <span className={styles.TriggerValue}>
                        {conditionsCount
                            ? t("MultiSelectFilter.selected", { count: conditionsCount })
                            : t("MultiSelectFilter.all")}
                    </span>
                    <span className={styles.Caret} aria-hidden="true">
                        ▾
                    </span>
                </Button>
            </Popover>

            {slice && hasRange && (
                <Tag closable onClose={clearRange} className={`TextUltraSmall ${styles.Selected}`}>
                    {`${platformFullName(slice.platform)} ${formatSpringSliceRange(slice.from, slice.to)}`}
                </Tag>
            )}

            {slice && hasStatus && (
                <Tag closable onClose={clearStatus} className={`TextUltraSmall ${styles.Selected}`}>
                    {slice.status === EFrameworkSupportStatus.OSS_SUPPORTED
                        ? t("Dashboard.SpringPortfolio.ossSupported")
                        : t("Dashboard.SpringPortfolio.windowClosed")}
                </Tag>
            )}
        </>
    );
};
