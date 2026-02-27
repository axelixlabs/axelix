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
import { App } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { Accordion, TooltipWithCopy } from "components";
import type { ILoggerGroup } from "models";
import { changeLoggerGroupLevel } from "services";

import { Levels } from "../Levels";

import styles from "./styles.module.css";

interface IProps {
    /**
     * All logger groups data
     */
    loggerGroups: ILoggerGroup[];

    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];

    /**
     * Fetches loggers data.
     */
    fetchLoggersData: () => void;
}

export const LoggerGroups = ({ loggerGroups, levels, fetchLoggersData }: IProps) => {
    // TODO: Add loading handler in future after fetchData and StatefulRequest refactoring
    const { t } = useTranslation();
    const { message } = App.useApp();

    const { instanceId } = useParams();

    const handleChange = (level: string, groupName: string): void => {
        changeLoggerGroupLevel({
            instanceId: instanceId!,
            configuredLevel: level,
            groupName: groupName,
        }).then(() => {
            message.success(t("Loggers.loggerLevelUpdated"));
            fetchLoggersData();
        });
    };

    return (
        <>
            <div className="AccordionsWrapper">
                {loggerGroups.map(({ name, members, configuredLevel }) => {
                    return (
                        <Accordion
                            header={
                                <div className={styles.AccordionHeader}>
                                    <TooltipWithCopy text={name} />
                                    <Levels
                                        levels={levels}
                                        configuredLevel={configuredLevel}
                                        checkedLevel={configuredLevel}
                                        handleChange={(level) => handleChange(level, name)}
                                    />
                                </div>
                            }
                            key={name}
                        >
                            {members.map((member) => (
                                <div className={styles.Member} key={member}>
                                    {member}
                                </div>
                            ))}
                        </Accordion>
                    );
                })}
            </div>
        </>
    );
};
