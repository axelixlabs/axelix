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
        <div id={normalizeHtmlElementId(headerName)} className={styles.MainWrapper}>
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
