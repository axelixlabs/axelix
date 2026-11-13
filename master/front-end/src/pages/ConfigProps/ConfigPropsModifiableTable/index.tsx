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
import { Accordion, Copy, EmptyHandler } from "@/components";
import { normalizeHtmlElementId } from "@/helpers";
import { EPropertyTriageTag, type IConfigPropsProperty } from "@/models";

import { RiskChip } from "../../Environment/EnvironmentModifiableTable/EnvironmentProperty/RiskChip";
import riskStyles from "../../Environment/EnvironmentModifiableTable/EnvironmentProperty/styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Table header name
     */
    headerName: string;

    /**
     * Config-props bean prefix
     */
    prefix: string;

    /**
     * Table rows data
     */
    properties: IConfigPropsProperty[];
}

export const ConfigPropsModifiableTable = ({ headerName, prefix, properties }: IProps) => {
    return (
        <>
            <div id={normalizeHtmlElementId(headerName)}>
                <div className={`AccordionsWrapper ${styles.AccordionWrapper}`}>
                    <Accordion
                        header={
                            <div className={styles.AccordionHeader}>
                                <div>{headerName}</div>
                                <div className={styles.Prefix}>
                                    <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
                                </div>
                            </div>
                        }
                        headerStyles={styles.HeaderStyles}
                        contentStyles={styles.ContentStyles}
                        accordionExpanded
                    >
                        <EmptyHandler isEmpty={!properties.length}>
                            {properties.map(({ key, value, dangerousValue }) => (
                                <div key={key} className={`TableRow ${dangerousValue ? riskStyles.DangerousRow : ""}`}>
                                    <div className={`RowChunk ${styles.KeyChunk}`}>
                                        {key} <Copy text={key} />
                                        {dangerousValue && (
                                            <RiskChip
                                                risk={{
                                                    kind: EPropertyTriageTag.DANGEROUS,
                                                    reason: dangerousValue.rationale,
                                                    hint: dangerousValue.alternativeExample ?? null,
                                                }}
                                            />
                                        )}
                                    </div>
                                    <div className={`RowChunk ${styles.ValueChunk}`}>
                                        <div>{value || "null"}</div>
                                    </div>
                                </div>
                            ))}
                        </EmptyHandler>
                    </Accordion>
                </div>
            </div>
        </>
    );
};
