import { Tabs, type TabsProps, message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterLoggerGroups, filterLoggers } from "helpers";
import { ELoggersTabs, type ILoggersResponseBody, StatefulRequest, StatelessRequest } from "models";
import { getLoggersData } from "services";

import { LoggerGroups } from "./LoggerGroups";
import { LoggersList } from "./LoggersList";
import styles from "./styles.module.css";

export const Loggers = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
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
        return loggersData.error;
    }

    const levels = loggersData.response!.levels;
    const loggerGroups = loggersData.response!.groups;
    const loggers = loggersData.response!.loggers;

    const effectiveLoggers = search ? filterLoggers(loggers, search) : loggers;
    const effectiveLoggerGroups = search ? filterLoggerGroups(loggerGroups, search) : loggerGroups;

    const loggersAddonAfter = `${effectiveLoggers.length} / ${loggers.length}`;
    const loggerGroupsAddonAffter = `${effectiveLoggerGroups.length} / ${loggerGroups.length}`;
    const addonAfter = activeKey === ELoggersTabs.LOGGERS ? loggersAddonAfter : loggerGroupsAddonAffter;

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
