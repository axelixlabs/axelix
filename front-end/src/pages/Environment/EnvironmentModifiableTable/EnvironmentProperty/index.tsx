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
import type { IEnvProperty } from "models";

import { EnvironmentPropertyValue } from "../../EnvironmentPropertyValue";
import sharedStyles from "../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;

    /**
     * Whether this row is in an even position in the shared list
     */
    isEvenElement: boolean;

    /**
     * Adds left padding to align with accordion rows that have an expand arrow
     */
    accordionAligned?: boolean;
}

export const EnvironmentProperty = ({ property, isEvenElement, accordionAligned }: IProps) => {
    const { name } = property;

    const rowBackgroundStyle = isEvenElement ? sharedStyles.EvenElement : sharedStyles.OddElement;

    return (
        <>
            <div
                className={`${styles.MainWrapper} ${rowBackgroundStyle} ${accordionAligned ? styles.AccordionAligned : ""}`}
            >
                <div className={styles.KeyChunk}>
                    <div className={styles.CopyableValue}>
                        {name} <Copy text={name} />
                    </div>
                </div>
                <div className={styles.ValueChunk}>
                    <EnvironmentPropertyValue property={property} />
                </div>
            </div>
        </>
    );
};
