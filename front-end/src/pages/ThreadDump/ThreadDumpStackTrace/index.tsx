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
                        <div key={traceLine} className={styles.lll}>
                            <p>{traceLine}</p>
                            {trace.nativeMethod && <Tag className={styles.NativeTag}>NATIVE</Tag>}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};
