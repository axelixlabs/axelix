import { Tabs, type TabsProps, message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData, filterLoggerGroups, filterLoggers, filterLoggersByLoggerName } from "helpers";
import {
    ELoggersTabs,
    type ILoggersResponseBody,
    type ILoggersSearchFilters,
    StatefulRequest,
    StatelessRequest,
} from "models";
import { getLoggersData } from "services";

import { Logger } from "./Logger";
import { LoggerGroups } from "./LoggerGroups";
import { LoggersSearchBar } from "./LoggersSearchBar";
import styles from "./styles.module.css";

export const Loggers = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const [activeKey, setActiveKey] = useState<ELoggersTabs>(ELoggersTabs.LOGGERS);

    const [loggersData, setLoggersData] = useState(StatefulRequest.loading<ILoggersResponseBody>());
    const [search, setSearch] = useState<string>("");
    const [updateLoggerLevel, setUpdateLoggerLevel] = useState(StatelessRequest.inactive());
    const [updateLoggerGroupLevel, setUpdateLoggerGroupLevel] = useState(StatelessRequest.inactive());
    const [searchFilters, setSearchFilters] = useState<ILoggersSearchFilters>({
        isConfiguredSearch: false,
        isClassOnlySearch: false,
    });

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

    const effectiveLoggers = isLoggersTab && search ? filterLoggersByLoggerName(loggers, search) : loggers;
    const loggersAfterFilters = filterLoggers(effectiveLoggers, searchFilters);

    const effectiveLoggerGroups = isLoggerGroupsTab && search ? filterLoggerGroups(loggerGroups, search) : loggerGroups;

    const loggersAddonAfter = `${loggersAfterFilters.length} / ${loggers.length}`;
    const loggerGroupsAddonAffter = `${effectiveLoggerGroups.length} / ${loggerGroups.length}`;
    const addonAfter = isLoggersTab ? loggersAddonAfter : loggerGroupsAddonAffter;

    const tabs: TabsProps["items"] = [
        {
            key: ELoggersTabs.LOGGERS,
            label: t("Loggers.loggers"),
            children: (
                <EmptyHandler isEmpty={loggersAfterFilters.length === 0}>
                    {loggersAfterFilters.map((logger) => (
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

        if (activeKey === ELoggersTabs.LOGGER_GROUPS) {
            setSearchFilters({
                isConfiguredSearch: false,
                isClassOnlySearch: false,
            });
        }
    };

    return (
        <>
            <div className={styles.FirstSection}>
                <LoggersSearchBar
                    activeKey={activeKey}
                    addonAfter={addonAfter}
                    searchFilters={searchFilters}
                    setSearch={setSearch}
                    setSearchFilters={setSearchFilters}
                />
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
