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
