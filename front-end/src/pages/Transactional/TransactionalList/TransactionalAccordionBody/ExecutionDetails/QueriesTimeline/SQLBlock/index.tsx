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
import { Light as SyntaxHighlighter } from "react-syntax-highlighter";
import sql from "react-syntax-highlighter/dist/esm/languages/hljs/sql";
import { arduinoLight } from "react-syntax-highlighter/dist/esm/styles/hljs";
import { format } from "sql-formatter";

import styles from "./styles.module.css";

SyntaxHighlighter.registerLanguage("sql", sql);

interface IProps {
    /**
     * SQL code needed for parsing
     */
    sql: string;
}

export const SQLBlock = ({ sql }: IProps) => {
    const formattedSQL = format(sql, {
        language: "postgresql",
        keywordCase: "upper",
        tabWidth: 2,
    });

    return (
        <div className={styles.MainWrapper}>
            <SyntaxHighlighter
                language="sql"
                style={arduinoLight}
                customStyle={{
                    backgroundColor: "transparent",
                }}
            >
                {formattedSQL}
            </SyntaxHighlighter>
        </div>
    );
};
