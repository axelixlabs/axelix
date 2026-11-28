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
import { Button, ConfigProvider, Slider, type ThemeConfig } from "antd";
import { useTranslation } from "react-i18next";

import { palette } from "@/theme/tokens";

import styles from "./styles.module.css";

const SLIDER_THEME: ThemeConfig = {
    components: {
        Slider: {
            railSize: 4,
            handleSize: 14,
            handleSizeHover: 16,
            handleLineWidth: 3,
            handleLineWidthHover: 3,
            railBg: palette.grey[200],
            railHoverBg: palette.grey[200],
            trackBg: palette.green[600],
            trackHoverBg: palette.green[600],
            handleColor: palette.green[600],
            handleActiveColor: palette.green[600],
        },
    },
};

interface IProps {
    versions: string[];
    from: string;
    to: string;
    onChange: (from: string, to: string) => void;
}

export const VersionRange = ({ versions, from, to, onChange }: IProps) => {
    const { t } = useTranslation();

    const fromIndex = versions.indexOf(from);
    const toIndex = versions.indexOf(to);
    const lastIndex = versions.length - 1;
    const pillWidth = `min(var(--tp-pill-width), (100% - 2 * var(--tp-pills-padding)) / ${versions.length})`;

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Header}>
                    <span className={`TextUltraSmall ${styles.Caption}`}>
                        {t("Dashboard.SpringPortfolio.versionRange")}
                    </span>
                    <Button
                        type="link"
                        size="small"
                        className={styles.AllVersions}
                        onClick={() => onChange(versions[0], versions[lastIndex])}
                    >
                        {t("Dashboard.SpringPortfolio.allVersions")}
                    </Button>
                </div>

                <div className={styles.Pills}>
                    <span
                        className={styles.Band}
                        aria-hidden="true"
                        style={{
                            left: `calc(var(--tp-pills-padding) + (100% - 2 * var(--tp-pills-padding) - ${pillWidth}) * ${fromIndex / (lastIndex || 1)})`,
                            width: `calc((100% - 2 * var(--tp-pills-padding) - ${pillWidth}) * ${(toIndex - fromIndex) / (lastIndex || 1)} + ${pillWidth})`,
                        }}
                    />
                    {versions.map((version, index) => {
                        const isEdge = index === fromIndex || index === toIndex;
                        const isInRange = index >= fromIndex && index <= toIndex;

                        return (
                            <button
                                key={version}
                                type="button"
                                aria-pressed={isEdge}
                                onClick={() => onChange(version, version)}
                                className={`TextUltraSmall ${styles.Pill} ${isInRange ? styles.InRange : ""} ${isEdge ? styles.Edge : ""}`}
                            >
                                {version}
                            </button>
                        );
                    })}
                </div>

                {lastIndex > 0 && (
                    <>
                        <ConfigProvider theme={SLIDER_THEME}>
                            <Slider
                                range
                                min={0}
                                max={lastIndex}
                                step={1}
                                value={[fromIndex, toIndex]}
                                tooltip={{ open: false }}
                                onChange={([nextFromIndex, nextToIndex]) =>
                                    onChange(versions[nextFromIndex], versions[nextToIndex])
                                }
                                style={{
                                    marginInline: `calc(var(--tp-pills-inset) + min(var(--tp-pill-width), (100% - 2 * var(--tp-pills-inset)) / ${versions.length}) / 2)`,
                                }}
                            />
                        </ConfigProvider>

                        <span className={`TextUltraSmall ${styles.Hint}`}>
                            {t("Dashboard.SpringPortfolio.versionRangeHint")}
                        </span>
                    </>
                )}
            </div>
        </>
    );
};
