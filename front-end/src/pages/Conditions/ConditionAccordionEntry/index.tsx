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
import { Accordion } from "components";
import { EConditionStatus, type ICondition } from "models";

import styles from "./styles.module.css";

import { CheckmarkIcon, CloseIcon } from "assets";

interface IStatusAwareCondition extends ICondition {
    status: EConditionStatus;
}

interface IProps {
    items: IStatusAwareCondition[];
}

export const ConditionsAccordionEntry = ({ items }: IProps) => {
    const findNeededIcon = (status: EConditionStatus) => {
        if (status === EConditionStatus.NOT_MATCHED) {
            return <CloseIcon />;
        }

        return <CheckmarkIcon />;
    };

    return (
        <>
            <div className={`AccordionsWrapper ${styles.AccordionsWrapper}`}>
                {items.map(({ message, condition, status }) => (
                    <Accordion
                        wrapperStyles={styles.AccordionWrapperStyles}
                        header={
                            <div className={styles.LabelWrapper}>
                                {findNeededIcon(status)}
                                {condition}
                            </div>
                        }
                        key={`${message} ${condition}`}
                    >
                        <div className={styles.Message}>{message}</div>
                    </Accordion>
                ))}
            </div>
        </>
    );
};
