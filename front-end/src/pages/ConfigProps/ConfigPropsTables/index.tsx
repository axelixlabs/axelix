import { useEffect } from "react";
import { useLocation } from "react-router-dom";

import { EmptyHandler, ModifiableTableSection } from "components";
import type { IConfigPropsBean } from "models";

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
                    <ModifiableTableSection
                        headerName={beanName}
                        properties={properties.map((property) => {
                            return {
                                key: `${prefix}.${property.key}`,
                                displayKey: property.key,
                                displayValue: property.value,
                            };
                        })}
                        key={beanName}
                        configPropsTable
                    >
                        {prefix && (
                            <div className={styles.Prefix}>
                                <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
                            </div>
                        )}
                    </ModifiableTableSection>
                ))}
            </>
        </EmptyHandler>
    );
};
