/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        <div>
            <div className={styles.Title}>{t("ThreadDump.stacktrace")}</div>
            <div>
                {stackTrace.map((trace) => {
                    const { moduleName, className, methodName, fileName, lineNumber } = trace;

                    const traceLine = `${moduleName ?? "unknown module"}:${className}.${methodName}(${fileName}: ${lineNumber})`;

                    return (
                        <div key={traceLine} className={styles.TraceLine}>
                            <p>{traceLine}</p>
                            {trace.nativeMethod && <Tag className={styles.NativeTag}>NATIVE</Tag>}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};
