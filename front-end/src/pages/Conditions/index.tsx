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
import { Tabs, type TabsProps } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useParams } from "react-router-dom";

import { EmptyHandler, HashNavigable, Loader, PageSearch } from "components";
import { fetchData, filterMatches } from "helpers";
import {
    type ConditionBeanCollection,
    EConditionsTabs,
    type IConditionBeanNegative,
    type IConditionBeanPositive,
    type IConditionsResponseBody,
    StatefulRequest,
} from "models";
import { getConditionsData } from "services";

import { Matches } from "./Matches";
import { NegativeConditions } from "./NegativeConditions";
import { PositiveConditions } from "./PositiveConditions";
import styles from "./styles.module.css";

const Conditions = () => {
    const { t } = useTranslation();
    const { hash } = useLocation();
    const { instanceId } = useParams();

    const [activeKey, setActiveKey] = useState<EConditionsTabs>(
        hash ? EConditionsTabs.POSITIVE_MATCHES : EConditionsTabs.NEGATIVE_MATCHES,
    );
    const [dataState, setDataState] = useState(StatefulRequest.loading<IConditionsResponseBody>());
    const [search, setSearch] = useState<string>("");

    useEffect(() => {
        fetchData(setDataState, () => getConditionsData(instanceId!));
    }, []);

    if (dataState.loading) {
        return <Loader />;
    }

    if (dataState.error) {
        return <EmptyHandler isEmpty />;
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
                    <HashNavigable className={styles.ConditionHeaderWrapper}>
                        <PositiveConditions positiveMatches={effectiveMatches as IConditionBeanPositive[]} />
                    </HashNavigable>
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
                <PageSearch addonAfter={addonAfter} setSearch={setSearch} key={activeKey} />

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
