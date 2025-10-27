import { message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, ModifiableTableSection, PageSearch } from "components";
import { fetchData, filterConfigPropsBeans } from "helpers";
import { useAppSelector } from "hooks";
import { type IConfigPropsResponseBody, StatefulRequest } from "models";
import { getConfigPropsData } from "services";

import styles from "./styles.module.css";

export const ConfigProps = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [search, setSearch] = useState<string>("");
    const [configProps, setConfigProps] = useState(StatefulRequest.loading<IConfigPropsResponseBody>());
    const updatePropertyState = useAppSelector((state) => state.updateProperty);

    const fetchConfigProps = (instanceId: string) => fetchData(setConfigProps, () => getConfigPropsData(instanceId));

    useEffect(() => {
        if (instanceId) {
            fetchConfigProps(instanceId);
        }
    }, []);

    useEffect(() => {
        if (updatePropertyState.completedSuccessfully() && instanceId) {
            fetchConfigProps(instanceId);
            message.success(t("saved"));
        }
    }, [updatePropertyState]);

    if (configProps.loading) {
        return <Loader />;
    }

    if (configProps.error) {
        // TODO: handle this case differently in the future
        return configProps.error;
    }

    const configPropsBeansFeed = configProps.response!.beans;

    const effectiveConfigProps = search ? filterConfigPropsBeans(configPropsBeansFeed, search) : configPropsBeansFeed;

    const addonAfter = `${effectiveConfigProps.length} / ${configPropsBeansFeed.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} search={search} setSearch={setSearch} />

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
        </>
    );
};

export default ConfigProps;
