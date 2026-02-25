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
import { App, Tabs, type TabsProps } from "antd";
import { type MouseEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterLoggerGroups, filterLoggers } from "helpers";
import { ELoggersTabs, type ILoggersResponseBody, StatefulRequest, StatelessRequest } from "models";
import { getLoggersData, resetLogger } from "services";

import { LoggerGroups } from "./LoggerGroups";
import { LoggersList } from "./LoggersList";
import styles from "./styles.module.css";

const Loggers = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [activeTab, setActiveTab] = useState<ELoggersTabs>(ELoggersTabs.LOGGERS);
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

    const isLoggersTab = activeTab === ELoggersTabs.LOGGERS;
    const isLoggerGroupsTab = activeTab === ELoggersTabs.LOGGER_GROUPS;

    const effectiveLoggers = isLoggersTab && search ? filterLoggers(loggers, search) : loggers;
    const effectiveLoggerGroups = isLoggerGroupsTab && search ? filterLoggerGroups(loggerGroups, search) : loggerGroups;

    const loggersAddonAfter = `${effectiveLoggers.length} / ${loggers.length}`;
    const loggerGroupsAddonAffter = `${effectiveLoggerGroups.length} / ${loggerGroups.length}`;
    const addonAfter = isLoggersTab ? loggersAddonAfter : loggerGroupsAddonAffter;

    const handleLoggerReset = (_: MouseEvent, loggerName: string): void => {
        resetLogger({
            instanceId: instanceId!,
            loggerName: loggerName,
        }).then(() => {
            message.success(t("Loggers.reset"));
            fetchLoggersData(instanceId!);
        });
    };

    const tabs: TabsProps["items"] = [
        {
            key: ELoggersTabs.LOGGERS,
            label: t("Loggers.loggers"),
            children: (
                <EmptyHandler isEmpty={effectiveLoggers.length === 0}>
                    <LoggersList
                        effectiveLoggers={effectiveLoggers}
                        levels={levels}
                        setUpdateLoggerLevel={setUpdateLoggerLevel}
                        handleReset={handleLoggerReset}
                    />
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
        setActiveTab(activeKey as ELoggersTabs);
    };

    return (
        <>
            <div className={styles.FirstSection}>
                <PageSearch addonAfter={addonAfter} setSearch={setSearch} key={activeTab} />
                <Tabs
                    activeKey={activeTab}
                    onChange={handleTabChange}
                    size="small"
                    items={tabs.map((tab) => ({ key: tab.key, label: tab.label }))}
                />
            </div>

            {tabs.find((tab) => tab.key === activeTab)!.children}
        </>
    );
};

export default Loggers;
