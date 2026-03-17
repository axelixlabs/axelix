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
import { Switch, Tooltip } from "antd";
import { useEffect, useRef, useState } from "react";

import styles from "./styles.module.css";
import { useTranslation } from "react-i18next";

interface IProps {
    MCPTool: any;
}

export const MCPCard = ({ MCPTool }: IProps) => {
    const { t } = useTranslation()

    const textRef = useRef<HTMLDivElement>(null);
    const [isEllipsis, setIsEllipsis] = useState<boolean>(false);

    const { title, description, viewAccessLog, annotation, isEnable } = MCPTool

    useEffect(() => {
        const element = textRef.current;

        if (!element) {
            return;
        }

        const checkTruncation = (): void => {
            setIsEllipsis(element.scrollHeight > element.clientHeight + 1);
        };

        const resizeObserver = new ResizeObserver(() => checkTruncation());

        resizeObserver.observe(element);
        checkTruncation();

        return () => {
            resizeObserver.disconnect();
        };
    }, []);

    return (
        <>
            <div className={styles.Card}>
                <div className={styles.Header}>
                    <div className={`TextSmall ${styles.RibbonWrapper}`}>
                        <span className={styles.Ribbon}>{viewAccessLog}</span>
                    </div>
                    <Tooltip title={isEllipsis ? title : undefined}>
                        <div ref={textRef} className={styles.Title}>
                            {title}
                        </div>
                    </Tooltip>
                </div>

                <div className={`TextSmall ${styles.Description}`}>
                    {description}
                </div>

                <div className={`TextSmall ${styles.Annotation}`}>
                    {annotation}
                </div>

                <div className={styles.Footer}>
                    <div>{isEnable ? t("MCP.enabled") : t("MCP.disabled")}</div>
                    <div className={styles.StatusSwitchWrapper}>
                        <Switch checked={isEnable} size="small" className={styles.StatusSwitch} />
                    </div>
                </div>
            </div>
        </>
    );
};
