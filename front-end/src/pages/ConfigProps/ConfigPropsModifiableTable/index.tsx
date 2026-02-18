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
import type { PropsWithChildren } from "react";

import { Accordion, Copy, EmptyHandler } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { ITableRow } from "models";

import { ConfigPropsPropertyValue } from "../ConfigPropsPropertyValue";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Table header name
     */
    headerName: string;

    /**
     * Table rows data
     */
    properties: ITableRow[];
}

export const ConfigPropsModifiableTable = ({ headerName, properties, children }: PropsWithChildren<IProps>) => {
    return (
        <div id={normalizeHtmlElementId(headerName)}>
            <div className={`AccordionsWrapper ${styles.AccordionWrapper}`}>
                <Accordion
                    header={
                        <div className={styles.AccordionHeader}>
                            <div>{headerName}</div>
                            {children}
                        </div>
                    }
                    headerStyles={styles.HeaderStyles}
                    contentStyles={styles.ContentStyles}
                    accordionExpanded
                >
                    <EmptyHandler isEmpty={!properties.length}>
                        {properties.map(({ key, displayKey, displayValue }) => (
                            <div key={key} className="TableRow">
                                <div className={`RowChunk ${styles.KeyChunk}`}>
                                    {displayKey} <Copy text={displayKey} />
                                </div>
                                <div className={`RowChunk ${styles.ValueChunk}`}>
                                    <ConfigPropsPropertyValue propertyName={key} propertyValue={displayValue} />
                                </div>
                            </div>
                        ))}
                    </EmptyHandler>
                </Accordion>
            </div>
        </div>
    );
};
