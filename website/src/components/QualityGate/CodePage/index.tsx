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
import { highlight } from "../highlight";
import { IQualityExample, SEVERITY_COLORS } from "../items";

import styles from "./styles.module.css";

interface IProps {
    example: IQualityExample;
}

const getSummary = ({ language, findings }: IQualityExample) => {
    const count = (severity: string) => findings.filter((finding) => finding.severity === severity).length;

    if (language === "xml") {
        return `${count("discontinued")} discontinued · ${count("maintenance")} maintenance · ${count("dormant")} dormant`;
    }
    return `${findings.length} findings · ${count("blocker")} blockers`;
};

export const CodePage = ({ example }: IProps) => {
    const { path, language, code, flags, findings } = example;

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <span className={styles.Path}>{path}</span>
                <span className={styles.Summary}>{getSummary(example)}</span>
            </div>
            <div className={styles.Lines}>
                {code.map((line, index) => {
                    const mark = flags[index + 1];
                    const colors = mark ? SEVERITY_COLORS[findings[mark - 1].severity] : null;

                    return (
                        <div
                            key={index}
                            className={styles.Line}
                            style={
                                colors
                                    ? {
                                          backgroundColor: `color-mix(in srgb, ${colors.mark} 12%, transparent)`,
                                          boxShadow: `inset 3px 0 0 ${colors.mark}`,
                                      }
                                    : undefined
                            }
                        >
                            <span className={styles.LineNumber}>
                                {colors ? (
                                    <span className={styles.Mark} style={{ backgroundColor: colors.mark }}>
                                        {mark}
                                    </span>
                                ) : (
                                    index + 1
                                )}
                            </span>
                            <span className={styles.Code}>
                                {highlight(language, line).map(({ text, color }, segmentIndex) => (
                                    <span key={segmentIndex} style={{ color }}>
                                        {text}
                                    </span>
                                ))}
                            </span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};
