import { EmptyHandler, ModifiableTableSection } from "components";
import type { IConfigPropsBean } from "models";

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
                >
                    {prefix && (
                        <div className={styles.Prefix}>
                            <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
                        </div>
                    )}
                </ModifiableTableSection>
            ))}
        </EmptyHandler>
    );
};
