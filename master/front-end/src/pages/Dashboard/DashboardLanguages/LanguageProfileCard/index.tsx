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
import { type ReactNode } from "react";

import type { ILanguagesDonutSegment, ILanguagesLegendRow } from "@/models";

import { LanguageDonut } from "../LanguageDonut";

import styles from "./styles.module.css";

interface IProps {
    eyebrow: string;
    title: string;
    descriptor: ReactNode;
    segments: ILanguagesDonutSegment[];
    centreTop: string;
    centreBottom: string;
    rows: ILanguagesLegendRow[];
    summary?: { label: string; value: string };
    footerLeft: ReactNode;
    footerRight?: string;
}

export const LanguageProfileCard = ({
    eyebrow,
    title,
    descriptor,
    segments,
    centreTop,
    centreBottom,
    rows,
    summary,
    footerLeft,
    footerRight,
}: IProps) => {
    return (
        <>
            <div className={styles.Card}>
                <div className={styles.Header}>
                    <div className={`TextUltraSmall ${styles.Eyebrow}`}>{eyebrow}</div>
                    <div className={styles.TitleRow}>
                        <div className={styles.Title}>{title}</div>
                        <div className={`TextUltraSmall ${styles.Descriptor}`}>{descriptor}</div>
                    </div>
                </div>

                <div className={styles.Body}>
                    <LanguageDonut segments={segments} centreTop={centreTop} centreBottom={centreBottom} />

                    <div className={`TextUltraSmall ${styles.Legend} DonutLegendFade`}>
                        {summary && (
                            <div className={styles.SummaryRow}>
                                <span className={styles.SummaryLabel}>{summary.label}</span>
                                <span className={styles.SummaryValue}>{summary.value}</span>
                            </div>
                        )}

                        {rows.map(({ color, label, cells }) => (
                            <div key={label} className={styles.Row}>
                                <span className={styles.LabelCell}>
                                    <span className={styles.Dot} style={{ backgroundColor: color }} />
                                    <span className={styles.Label}>{label}</span>
                                </span>
                                {cells.map((cell, index) => (
                                    <span
                                        key={index}
                                        className={`${styles.Cell} ${index === cells.length - 1 ? styles.CellStrong : ""}`}
                                    >
                                        {cell}
                                    </span>
                                ))}
                            </div>
                        ))}
                    </div>
                </div>

                <div className={`TextUltraSmall ${styles.FooterWrapper}`}>
                    <div>{footerLeft}</div>
                    {footerRight && <div className={styles.FooterMono}>{footerRight}</div>}
                </div>
            </div>
        </>
    );
};
