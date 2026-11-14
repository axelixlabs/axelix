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
import styles from "../shared.module.css";

interface IProps {
    code: string;
    language: "xml" | "gradle";
}

// These patterns cover the Maven and Gradle examples shown by the installer.
export const BuildSnippet = ({ code, language }: IProps) => {
    const pattern =
        language === "xml" ? /(<\/?[\w.-]+>)/g : /("[^"\n]*"|'[^'\n]*'|\b(?:implementation|plugins|id|version)\b)/g;

    return (
        <code className={language === "xml" ? styles.St : undefined}>
            {code.split(pattern).map((text, index) => {
                if (index % 2 === 0) {
                    return text;
                }

                const tokenStyle = language === "xml" ? styles.Tg : /^["']/.test(text) ? styles.St : styles.Kw;
                return (
                    <span key={index} className={tokenStyle}>
                        {text}
                    </span>
                );
            })}
        </code>
    );
};
