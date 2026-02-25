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
import type { AxiosError } from "axios";
import type { Dispatch, SetStateAction } from "react";
import { useParams } from "react-router-dom";

import { Accordion, TooltipWithCopy } from "components";
import { extractErrorCode } from "helpers";
import { type IErrorResponse, type ILoggerGroup, StatelessRequest } from "models";
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
     * State responsible for updating the group logger level
     */
    setUpdateLoggerGroupLevel: Dispatch<SetStateAction<StatelessRequest>>;
}

export const LoggerGroups = ({ loggerGroups, levels, setUpdateLoggerGroupLevel }: IProps) => {
    const { instanceId } = useParams();

    return (
        <>
            <div className="AccordionsWrapper">
                {loggerGroups.map(({ name, members, configuredLevel }) => {
                    const handleChange = (selectedLevel: string): void => {
                        setUpdateLoggerGroupLevel(StatelessRequest.loading());

                        changeLoggerGroupLevel({
                            instanceId: instanceId!,
                            configuredLevel: selectedLevel,
                            groupName: name,
                        })
                            .then((value) => {
                                if (value.status === 200) {
                                    setUpdateLoggerGroupLevel(StatelessRequest.success());
                                } else {
                                    setUpdateLoggerGroupLevel(StatelessRequest.error(""));
                                }
                            })
                            .catch((error: AxiosError<IErrorResponse>) => {
                                setUpdateLoggerGroupLevel(
                                    StatelessRequest.error(extractErrorCode(error?.response?.data)),
                                );
                            });
                    };

                    return (
                        <>
                            <Accordion
                                header={
                                    <div className={styles.AccordionHeader}>
                                        <TooltipWithCopy text={name} />
                                        <Levels
                                            levels={levels}
                                            configuredLevel={configuredLevel}
                                            checkedLevel={configuredLevel}
                                            handleChange={handleChange}
                                        />
                                    </div>
                                }
                                key={name}
                            >
                                <>
                                    {members.map((member) => (
                                        <div className={styles.Member} key={member}>
                                            {member}
                                        </div>
                                    ))}
                                </>
                            </Accordion>
                        </>
                    );
                })}
            </div>
        </>
    );
};
