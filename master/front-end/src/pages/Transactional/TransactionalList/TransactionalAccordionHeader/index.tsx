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
import { Copy } from "components";
import type { ITransactionalEntryPoint } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single transactional data
     */
    transactional: ITransactionalEntryPoint;
}

export const TransactionalAccordionHeader = ({ transactional }: IProps) => {
    const { className, methodName } = transactional;

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.HeaderWrapper}>
                    <div className={styles.LabelWrapper}>
                        <span className={styles.Label}>Class:</span>
                        <span>{className}</span>
                        <Copy text={className} />
                    </div>
                    <div className={styles.LabelWrapper}>
                        <span className={styles.Label}>Method:</span>
                        <span>{methodName}</span>
                        <Copy text={methodName} />
                    </div>
                </div>
            </div>
        </>
    );
};
