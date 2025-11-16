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
                            setUpdateLoggerGroupLevel(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                        });
                };

                return (
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
                );
            })}
        </div>
    );
};
