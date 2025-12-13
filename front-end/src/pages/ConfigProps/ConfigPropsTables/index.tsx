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
import { useEffect } from "react";
import { useLocation } from "react-router-dom";

import { EmptyHandler } from "components";
import type { IConfigPropsBean } from "models";

import accordionStyles from "../../../components/Accordion/styles.module.css";
import { ConfigPropsModifiableTable } from "../ConfigPropsModifiableTable";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The list of config props
     */
    effectiveConfigProps: IConfigPropsBean[];
    /**
     * If true, a request is made to fetch the config props data
     */
    loading: boolean;
}

function scrollToConfigPropsBean(hash: string) {
    const element = document.querySelector(hash);
    if (element) {
        element.scrollIntoView();

        const header = element.querySelector(`.${accordionStyles.HeaderWrapper}`);
        if (header) {
            header.classList.add(styles.Highlight);
        }
    }
}

export const ConfigPropsTables = ({ effectiveConfigProps, loading }: IProps) => {
    const { hash } = useLocation();

    // TODO:
    //  We have to use useEffect hook here since the page is rendered by the browser initially
    //  when there is no data yet backing the configprops table. Therefore, there is no element
    //  with the requested 'hash', and thus the browser simply cannot navigate to the element that
    //  is just not yet loaded from the backend. Once the data is loaded, the browser will not re-attempt
    //  to re-navigate to the requested 'hash', and therefore we have to do it manually here.
    useEffect(() => {
        if (!loading && hash) {
            scrollToConfigPropsBean(hash);
        }
    }, [loading, hash]);

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
