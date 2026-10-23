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
import { IQualityExample, SEVERITY_COLORS } from "../items";

import styles from "./styles.module.css";

interface IProps {
    example: IQualityExample;
}

export const FindingsPage = ({ example: { kicker, file, title, intro, findings } }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Kicker}>
                {kicker} · {file}
            </span>
            <div className={styles.Title}>{title}</div>
            <div className={styles.Intro}>{intro}</div>
            <div className={styles.List}>
                {findings.map(({ rule, severity, body }, index) => {
                    const colors = SEVERITY_COLORS[severity];

                    return (
                        <div key={rule} className={styles.Finding}>
                            <span className={styles.Mark} style={{ backgroundColor: colors.mark }}>
                                {index + 1}
                            </span>
                            <div className={styles.FindingText}>
                                <div className={styles.FindingHeader}>
                                    <span className={styles.Rule}>{rule}</span>
                                    <span
                                        className={styles.Severity}
                                        style={{ backgroundColor: colors.background, color: colors.text }}
                                    >
                                        {severity}
                                    </span>
                                </div>
                                <span className={styles.Body}>{body}</span>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};
