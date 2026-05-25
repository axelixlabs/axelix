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
import { Tag } from "antd";
import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the thread dump.
     */
    threadDump: IThread;
}

export const ThreadDumpStackTrace = ({ threadDump }: IProps) => {
    const { t } = useTranslation();
    const { stackTrace } = threadDump;

    if (!stackTrace.length) {
        return <></>;
    }

    return (
        <>
            <div>
                <div className={styles.Title}>{t("ThreadDump.stacktrace")}</div>
                <div>
                    {stackTrace.map((trace, index) => {
                        const { moduleName, className, methodName, fileName, lineNumber } = trace;

                        const traceLine = `${moduleName ?? "unknown module"}:${className}.${methodName}(${fileName}: ${lineNumber})`;

                        return (
                            <div key={`${traceLine}${index}`} className={styles.TraceLine}>
                                <p>{traceLine}</p>
                                {trace.nativeMethod && <Tag className={styles.NativeTag}>NATIVE</Tag>}
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
