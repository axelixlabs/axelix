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
        <EmptyHandler isEmpty={effectiveConfigProps.length === 0}>
            <>
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
            </>
        </EmptyHandler>
    );
};
