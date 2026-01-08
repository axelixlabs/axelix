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
import { App, Tabs, type TabsProps } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterLoggerGroups, filterLoggers } from "helpers";
import { ELoggersTabs, type ILoggersResponseBody, StatefulRequest, StatelessRequest } from "models";
import { getLoggersData } from "services";

import { Logger } from "./Logger";
import { LoggerGroups } from "./LoggerGroups";
import styles from "./styles.module.css";

const Loggers = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [activeKey, setActiveKey] = useState<ELoggersTabs>(ELoggersTabs.LOGGERS);
    const [loggersData, setLoggersData] = useState(StatefulRequest.loading<ILoggersResponseBody>());
    const [search, setSearch] = useState<string>("");
    const [updateLoggerLevel, setUpdateLoggerLevel] = useState(StatelessRequest.inactive());
    const [updateLoggerGroupLevel, setUpdateLoggerGroupLevel] = useState(StatelessRequest.inactive());

    const fetchLoggersData = (instanceId: string) => fetchData(setLoggersData, () => getLoggersData(instanceId));

    const isLoggerLevelUpdated = updateLoggerLevel.completedSuccessfully();
    const isLoggerGroupLevelUpdated = updateLoggerGroupLevel.completedSuccessfully();

    useEffect(() => {
        fetchLoggersData(instanceId!);
    }, []);

    useEffect(() => {
        if (isLoggerLevelUpdated || isLoggerGroupLevelUpdated) {
            message.success(t("Loggers.loggerLevelUpdated"));
            fetchLoggersData(instanceId!);
            setUpdateLoggerLevel(StatelessRequest.inactive());
            setUpdateLoggerGroupLevel(StatelessRequest.inactive());
        }
    }, [isLoggerLevelUpdated, isLoggerGroupLevelUpdated]);

    if (loggersData.loading || updateLoggerLevel.loading || updateLoggerGroupLevel.loading) {
        return <Loader />;
    }

    if (loggersData.error) {
        return <EmptyHandler isEmpty />;
    }

    const levels = loggersData.response!.levels;
    const loggerGroups = loggersData.response!.groups;
    const loggers = loggersData.response!.loggers;

    const isLoggersTab = activeKey === ELoggersTabs.LOGGERS;
    const isLoggerGroupsTab = activeKey === ELoggersTabs.LOGGER_GROUPS;

    const effectiveLoggers = isLoggersTab && search ? filterLoggers(loggers, search) : loggers;
    const effectiveLoggerGroups = isLoggerGroupsTab && search ? filterLoggerGroups(loggerGroups, search) : loggerGroups;

    const loggersAddonAfter = `${effectiveLoggers.length} / ${loggers.length}`;
    const loggerGroupsAddonAffter = `${effectiveLoggerGroups.length} / ${loggerGroups.length}`;
    const addonAfter = isLoggersTab ? loggersAddonAfter : loggerGroupsAddonAffter;

    const tabs: TabsProps["items"] = [
        {
            key: ELoggersTabs.LOGGERS,
            label: t("Loggers.loggers"),
            children: (
                <EmptyHandler isEmpty={effectiveLoggers.length === 0}>
                    {effectiveLoggers.map((logger) => (
                        <Logger
                            logger={logger}
                            levels={levels}
                            setUpdateLoggerLevel={setUpdateLoggerLevel}
                            key={logger.name}
                        />
                    ))}
                </EmptyHandler>
            ),
        },
        {
            key: ELoggersTabs.LOGGER_GROUPS,
            label: t("Loggers.loggerGroups"),
            children: (
                <EmptyHandler isEmpty={effectiveLoggerGroups.length === 0}>
                    <LoggerGroups
                        loggerGroups={effectiveLoggerGroups}
                        levels={levels}
                        setUpdateLoggerGroupLevel={setUpdateLoggerGroupLevel}
                    />
                </EmptyHandler>
            ),
        },
    ];

    const handleTabChange = (activeKey: string): void => {
        setSearch("");
        setActiveKey(activeKey as ELoggersTabs);
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

            {tabs.find((tab) => tab.key === activeKey)!.children}
        </>
    );
};

export default Loggers;
