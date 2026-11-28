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
import { ArrowRightOutlined, CloseOutlined, FilterOutlined } from "@ant-design/icons";

import { Button, Segmented } from "antd";
import { type CSSProperties, useState } from "react";
import { useTranslation } from "react-i18next";

import { getSpringSliceVersions, getSpringSliceVersionsCount, platformFullName } from "@/helpers";
import {
    EFrameworkSupportStatus,
    EPlatformName,
    type ISpringPortfolioApplication,
    type ISpringSlice,
    type TSpringSliceStatus,
} from "@/models";
import { PLATFORM_ORDER } from "@/utils";

import { OssChip } from "../../../OssChip";

import { VersionRange } from "./VersionRange";
import styles from "./styles.module.css";

interface IProps {
    applications: ISpringPortfolioApplication[];
    slice: ISpringSlice | null;
    onApply: (slice: ISpringSlice) => void;
    onClear: () => void;
    onClose: () => void;
}

export const SpringSliceForm = ({ applications, slice, onApply, onClear, onClose }: IProps) => {
    const { t } = useTranslation();

    const [draft, setDraft] = useState<ISpringSlice>(() => {
        const versions = getSpringSliceVersions(applications, EPlatformName.SPRING_BOOT);

        return (
            slice ?? {
                platform: EPlatformName.SPRING_BOOT,
                from: versions[0],
                to: versions[versions.length - 1],
                status: "ALL",
            }
        );
    });

    const changePlatform = (platform: EPlatformName): void => {
        const versions = getSpringSliceVersions(applications, platform);

        setDraft({ ...draft, platform, from: versions[0], to: versions[versions.length - 1] });
    };

    return (
        <>
            <div
                className={styles.MainWrapper}
                style={{ "--tp-versions-count": getSpringSliceVersionsCount(applications) } as CSSProperties}
            >
                <div className={styles.Header}>
                    <span className={styles.HeaderIcon}>
                        <FilterOutlined />
                    </span>
                    <div className={styles.HeaderText}>
                        <span className={`TextSmall ${styles.Title}`}>
                            {t("Dashboard.SpringPortfolio.springSlice")}
                        </span>
                        <span className={`TextUltraSmall ${styles.Description}`}>
                            {t("Dashboard.SpringPortfolio.springSliceDescription")}
                        </span>
                    </div>
                    <Button
                        type="text"
                        icon={<CloseOutlined />}
                        aria-label={t("Dashboard.SpringPortfolio.close")}
                        onClick={onClose}
                    />
                </div>

                <div className={styles.Body}>
                    <Segmented<EPlatformName>
                        block
                        value={draft.platform}
                        onChange={changePlatform}
                        options={PLATFORM_ORDER.map((platform) => ({
                            value: platform,
                            label: platformFullName(platform),
                        }))}
                    />

                    <VersionRange
                        versions={getSpringSliceVersions(applications, draft.platform)}
                        from={draft.from}
                        to={draft.to}
                        onChange={(from, to) => setDraft({ ...draft, from, to })}
                    />

                    <div className={styles.Divider} aria-hidden="true">
                        <span className={styles.DividerLabel}>{t("Dashboard.SpringPortfolio.and")}</span>
                    </div>

                    <div className={styles.Field}>
                        <span className={`TextUltraSmall ${styles.Caption}`}>
                            {t("Dashboard.SpringPortfolio.columns.status")}
                        </span>
                        <Segmented<TSpringSliceStatus>
                            block
                            value={draft.status}
                            onChange={(status) => setDraft({ ...draft, status })}
                            options={[
                                { value: "ALL", label: t("Dashboard.SpringPortfolio.allStatuses") },
                                {
                                    value: EFrameworkSupportStatus.OSS_SUPPORTED,
                                    label: <OssChip ossSupported className={styles.StatusChip} />,
                                    title: t("Dashboard.SpringPortfolio.ossSupported"),
                                },
                                {
                                    value: EFrameworkSupportStatus.OUT_OF_OSS_MAINTENANCE,
                                    label: <OssChip ossSupported={false} className={styles.StatusChip} />,
                                    title: t("Dashboard.SpringPortfolio.windowClosed"),
                                },
                            ]}
                        />
                    </div>
                </div>

                <div className={styles.Footer}>
                    <Button type="link" className={styles.Clear} onClick={onClear}>
                        {t("Dashboard.SpringPortfolio.clear")}
                    </Button>
                    <Button
                        type="primary"
                        icon={<ArrowRightOutlined />}
                        iconPlacement="end"
                        onClick={() => onApply(draft)}
                    >
                        {t("Dashboard.SpringPortfolio.show")}
                    </Button>
                </div>
            </div>
        </>
    );
};
