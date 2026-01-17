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
import { Tooltip } from "antd";
import { useEffect, useRef, useState } from "react";

import { Copy } from "../Copy";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Tooltip text
     */
    text: string;
}

export const TooltipWithCopy = ({ text }: IProps) => {
    const textRef = useRef<HTMLDivElement>(null);
    const [isEllipsis, setIsEllipsis] = useState<boolean>(false);

    useEffect(() => {
        const text = textRef.current;
        if (text) {
            setIsEllipsis(text.scrollWidth > text.clientWidth);
        }
    }, [text]);

    return (
        <>
            <Tooltip title={isEllipsis ? text : undefined}>
                <div className={styles.TextWrapper}>
                    <div className={styles.Text} ref={textRef}>
                        {text}
                    </div>
                    <Copy text={text} />
                </div>
            </Tooltip>
        </>
    );
};
