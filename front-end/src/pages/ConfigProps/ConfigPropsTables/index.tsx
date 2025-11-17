import { useEffect } from "react";
import { useLocation } from "react-router-dom";

import { EmptyHandler } from "components";
import type { IConfigPropsBean } from "models";

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
            const elementToScroll = document.querySelector(hash);

            if (elementToScroll) {
                elementToScroll.scrollIntoView();
            }
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
