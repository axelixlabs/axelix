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
import { Tabs } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { EmptyHandler, Loader, PageSearch } from "@/components";
import { fetchData, filterLoggerGroups, filterLoggers } from "@/helpers";
import { ELoggersTabs, type ILoggersResponseBody, StatefulRequest } from "@/models";
import { getLoggersData } from "@/services";
import { loggersTabs } from "@/utils";

import { LoggersContent } from "./LoggersContent";
import styles from "./styles.module.css";

const Loggers = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [activeTab, setActiveTab] = useState<ELoggersTabs>(ELoggersTabs.LOGGERS);
    const [loggersData, setLoggersData] = useState(StatefulRequest.loading<ILoggersResponseBody>());
    const [search, setSearch] = useState<string>("");

    const setLoading = (loading: boolean): void => {
        setLoggersData((prev) => new StatefulRequest<ILoggersResponseBody>(loading, prev.error, prev.response));
    };

    const fetchLoggersData = (): void => {
        setLoading(true);
        fetchData(setLoggersData, () => getLoggersData(instanceId!));
    };

    const isLoading = loggersData.loading;

    useEffect(() => {
        fetchLoggersData();
    }, []);

    if (isLoading && !loggersData.response) {
        return <Loader />;
    }

    if (loggersData.error) {
        return <EmptyHandler isEmpty />;
    }

    const loggersResponse = loggersData.response!;
    const { levels, loggers, groups: loggerGroups } = loggersResponse;

    const isLoggersTab = activeTab === ELoggersTabs.LOGGERS;
    const isLoggerGroupsTab = activeTab === ELoggersTabs.LOGGER_GROUPS;

    const effectiveLoggers = isLoggersTab && search ? filterLoggers(loggers, search) : loggers;
    const effectiveLoggerGroups = isLoggerGroupsTab && search ? filterLoggerGroups(loggerGroups, search) : loggerGroups;

    const loggersAddonAfter = `${effectiveLoggers.length} / ${loggers.length}`;
    const loggerGroupsAddonAfter = `${effectiveLoggerGroups.length} / ${loggerGroups.length}`;
    const addonAfter = isLoggersTab ? loggersAddonAfter : loggerGroupsAddonAfter;

    const handleTabChange = (activeKey: string): void => {
        setSearch("");
        setActiveTab(activeKey as ELoggersTabs);
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                {isLoading && (
                    <div className={styles.Overlay}>
                        <Loader />
                    </div>
                )}

                <div className={styles.FirstSection}>
                    <PageSearch addonAfter={addonAfter} setSearch={setSearch} key={activeTab} />
                    <Tabs activeKey={activeTab} onChange={handleTabChange} size="small" items={loggersTabs(t)} />
                </div>

                <LoggersContent
                    activeTab={activeTab}
                    effectiveLoggerGroups={effectiveLoggerGroups}
                    effectiveLoggers={effectiveLoggers}
                    fetchLoggersData={fetchLoggersData}
                    levels={levels}
                    setLoading={setLoading}
                />
            </div>
        </>
    );
};

export default Loggers;
