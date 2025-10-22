import { Collapse, type CollapseProps } from "antd";
import { useState } from "react";

import { EConditionStatus, type ICondition } from "models";

import styles from "./styles.module.css";

import Checkmark from "assets/icons/checkmark.svg";
import Close from "assets/icons/close.svg";

interface IStatusAwareCondition extends ICondition {
    status: EConditionStatus;
}

interface IProps {
    items: IStatusAwareCondition[];
}

export const ConditionsAccordionEntry = ({ items }: IProps) => {
    const [activeKey, setActiveKey] = useState<string | string[]>([]);

    const findNeededIcon = (status: EConditionStatus) => {
        if (status === EConditionStatus.NOT_MATCHED) {
            return <img src={Close} alt="Close icon" />;
        }

        return <img src={Checkmark} alt="Checkmark icon" />;
    };

    const createCollapseItems = (): CollapseProps["items"] =>
        items.map(({ message, condition, status }) => ({
            key: `${message} ${condition}`,
            label: (
                <div className={styles.LabelWrapper}>
                    {findNeededIcon(status)}
                    {condition}
                </div>
            ),
            children: <div className={styles.Message}>{message}</div>,
        }));

    return (
        <Collapse
            accordion
            activeKey={activeKey}
            items={createCollapseItems()}
            onChange={(key) => setActiveKey(key)}
            bordered
            className={`Collapse ${styles.Collapse}`}
        />
    );
};
