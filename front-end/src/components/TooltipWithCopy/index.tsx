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
