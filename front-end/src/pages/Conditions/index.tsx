import { Tabs, type TabsProps } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterMatches } from "helpers";
import {
    type ConditionBeanCollection,
    EConditionsTabs,
    type IConditionBeanNegative,
    type IConditionBeanPositive,
    type IConditionsData,
    StatefulRequest,
} from "models";
import { getConditionsData } from "services";

import { Matches } from "./Matches";
import { NegativeConditions } from "./NegativeConditions";
import { PositiveConditions } from "./PositiveConditions";
import styles from "./styles.module.css";

export const Conditions = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [activeKey, setActiveKey] = useState<EConditionsTabs>(EConditionsTabs.NEGATIVE_MATCHES);
    const [dataState, setDataState] = useState(StatefulRequest.loading<IConditionsData>());
    const [search, setSearch] = useState<string>("");

    useEffect(() => {
        fetchData(setDataState, () => getConditionsData(instanceId!));
    }, []);

    if (dataState.loading) {
        return <Loader />;
    }

    if (dataState.error) {
        return dataState.error;
    }

    const negativeMatches = dataState.response!.negativeMatches;
    const positiveMatches = dataState.response!.positiveMatches;

    const isNegativeTab = activeKey === EConditionsTabs.NEGATIVE_MATCHES;

    const matches: ConditionBeanCollection = isNegativeTab ? negativeMatches : positiveMatches;

    const effectiveMatches: ConditionBeanCollection = search ? filterMatches(matches, search) : matches;
    const addonAfter = `${effectiveMatches.length} / ${matches.length}`;

    const tabs: TabsProps["items"] = [
        {
            key: EConditionsTabs.NEGATIVE_MATCHES,
            label: t("Conditions.negativeMatches"),
            children: (
                <Matches title={t("Conditions.negativeMatches")}>
                    <NegativeConditions negativeMatches={effectiveMatches as IConditionBeanNegative[]} />
                </Matches>
            ),
        },
        {
            key: EConditionsTabs.POSITIVE_MATCHES,
            label: t("Conditions.positiveMatches"),
            children: (
                <Matches title={t("Conditions.positiveMatches")}>
                    <PositiveConditions positiveMatches={effectiveMatches as IConditionBeanPositive[]} />
                </Matches>
            ),
        },
    ];

    const handleTabChange = (activeKey: string): void => {
        setSearch("");
        setActiveKey(activeKey as EConditionsTabs);
    };

    return (
        <>
            <div className={styles.FirstSection}>
                <PageSearch addonAfter={addonAfter} search={search} setSearch={setSearch} />
                <Tabs
                    activeKey={activeKey}
                    onChange={handleTabChange}
                    size="small"
                    items={tabs.map((tab) => ({ key: tab.key, label: tab.label }))}
                />
            </div>

            <EmptyHandler isEmpty={!effectiveMatches.length}>
                {tabs.find((tab) => tab.key === activeKey)!.children}
            </EmptyHandler>
        </>
    );
};

export default Conditions;
