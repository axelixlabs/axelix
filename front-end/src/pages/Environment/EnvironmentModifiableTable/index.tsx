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
import { Link, useParams } from "react-router-dom";

import { Accordion, Copy, EmptyHandler, TablePropertyValue } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { IEnvironmentTableRow } from "models";

import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * Table header name
     */
    headerName: string;

    /**
     * Table rows data
     */
    properties: IEnvironmentTableRow[];
}

export const EnvironmentModifiableTable = ({ headerName, properties }: IProps) => {
    const { instanceId } = useParams();

    return (
        <div className={`AccordionsWrapper ${styles.AccordionWrapper}`}>
            <Accordion
                header={<div className={styles.AccordionHeader}>{headerName}</div>}
                headerStyles={styles.HeaderStyles}
                contentStyles={styles.ContentStyles}
                accordionExpanded
            >
                <EmptyHandler isEmpty={!properties.length}>
                    {properties.map(({ key, displayKey, displayValue, isPrimary, configPropsBeanName }) => (
                        <div key={key} className="TableRow">
                            <div className={`RowChunk ${styles.KeyChunk}`}>
                                {displayKey} <Copy text={displayKey} />
                                {configPropsBeanName && (
                                    <Link
                                        to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(configPropsBeanName)}`}
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        <img src={LinkIcon} alt="Link icon" />
                                    </Link>
                                )}
                            </div>
                            <div className={`RowChunk ${styles.ValueChunk}`}>
                                <TablePropertyValue
                                    propertyName={key}
                                    propertyValue={displayValue}
                                    isPrimary={isPrimary}
                                />
                            </div>
                        </div>
                    ))}
                </EmptyHandler>
            </Accordion>
        </div>
    );
};
