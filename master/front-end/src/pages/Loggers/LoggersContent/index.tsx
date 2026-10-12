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
import { Activity } from "react";

import { EmptyHandler } from "@/components";
import { ELoggersTabs, type ILogger, type ILoggerGroup } from "@/models";

import { LoggerGroups } from "./LoggerGroups";
import { LoggersList } from "./LoggersList";

interface IProps {
    /**
     * The tab that is currently active
     */
    activeTab: ELoggersTabs;

    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];

    /**
     * Loggers after the search filter
     */
    effectiveLoggers: ILogger[];

    /**
     * Logger groups after the search filter
     */
    effectiveLoggerGroups: ILoggerGroup[];

    /**
     * Fetches loggers data.
     */
    fetchLoggersData: () => void;

    /**
     * Sets the loading state.
     */
    setLoading: (loading: boolean) => void;
}

export const LoggersContent = ({
    activeTab,
    levels,
    effectiveLoggers,
    effectiveLoggerGroups,
    fetchLoggersData,
    setLoading,
}: IProps) => {
    const isLoggersTab = activeTab === ELoggersTabs.LOGGERS;
    const isLoggerGroupsTab = activeTab === ELoggersTabs.LOGGER_GROUPS;

    return (
        <>
            <Activity mode={isLoggersTab ? "visible" : "hidden"}>
                <EmptyHandler isEmpty={effectiveLoggers.length === 0}>
                    <LoggersList
                        effectiveLoggers={effectiveLoggers}
                        levels={levels}
                        fetchLoggersData={fetchLoggersData}
                        setLoading={setLoading}
                    />
                </EmptyHandler>
            </Activity>

            <Activity mode={isLoggerGroupsTab ? "visible" : "hidden"}>
                <EmptyHandler isEmpty={effectiveLoggerGroups.length === 0}>
                    <LoggerGroups
                        loggerGroups={effectiveLoggerGroups}
                        levels={levels}
                        fetchLoggersData={fetchLoggersData}
                        setLoading={setLoading}
                    />
                </EmptyHandler>
            </Activity>
        </>
    );
};
