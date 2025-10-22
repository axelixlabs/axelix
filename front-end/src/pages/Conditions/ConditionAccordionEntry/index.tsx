import { Collapse, type CollapseProps } from "antd"
import { EConditionStatus, type ICondition, } from "models";
import { useState } from "react";

import Close from 'assets/icons/close.svg'
import Checkmark from 'assets/icons/checkmark.svg'

import styles from './styles.module.css'

interface IProps {
    items: ICondition[]
}

export const ConditionsAccordionEntry = ({ items }: IProps) => {
    const [activeKey, setActiveKey] = useState<string | string[]>([]);

    const findNeededIcon = (status: EConditionStatus) => {
        if (status === EConditionStatus.NOT_MATCHED) {
            return <img src={Close} alt="Close icon" />
        }

        return <img src={Checkmark} alt="Checkmark icon" />
    }

    const createCollapseItems = (): CollapseProps["items"] => (
        items.map(({ message, condition, status }) => ({
            key: `${message} ${condition}`,
            label: (
                <div className={styles.LabelWrapper}>
                    {!!status && findNeededIcon(status)}
                    {condition}
                </div>
            ),
            children: <div className={styles.Message}>{message}</div>
        }))
    )

    return (
        <Collapse
            accordion
            activeKey={activeKey}
            items={createCollapseItems()}
            onChange={(key) => setActiveKey(key)}
            bordered
            className={`Collapse ${styles.Collapse}`}
        />
    )
}