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
import { EmptyHandler } from "components";
import type { IConfigPropsBean } from "models";

import { ConfigPropsModifiableTable } from "../ConfigPropsModifiableTable";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The list of config props
     */
    effectiveConfigProps: IConfigPropsBean[];
}

export const ConfigPropsTables = ({ effectiveConfigProps }: IProps) => {
    return (
        <>
            <EmptyHandler isEmpty={effectiveConfigProps.length === 0}>
                {effectiveConfigProps.map(({ beanName, prefix, properties }) => (
                    <ConfigPropsModifiableTable
                        headerName={beanName}
                        properties={properties.map((property) => {
                            return {
                                key: `${prefix}.${property.key}`,
                                displayKey: property.key,
                                displayValue: property.value,
                            };
                        })}
                        key={beanName}
                    >
                        {prefix && (
                            <div className={styles.Prefix}>
                                <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
                            </div>
                        )}
                    </ConfigPropsModifiableTable>
                ))}
            </EmptyHandler>
        </>
    );
};
