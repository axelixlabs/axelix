import { Accordion } from "components";
import { EConditionStatus, type ICondition } from "models";

import styles from "./styles.module.css";

import CheckmarkIcon from "assets/icons/checkmark.svg";
import CloseIcon from "assets/icons/close.svg";

interface IStatusAwareCondition extends ICondition {
    status: EConditionStatus;
}

interface IProps {
    items: IStatusAwareCondition[];
}

export const ConditionsAccordionEntry = ({ items }: IProps) => {
    const findNeededIcon = (status: EConditionStatus) => {
        if (status === EConditionStatus.NOT_MATCHED) {
            return <img src={CloseIcon} alt="Close icon" />;
        }

        return <img src={CheckmarkIcon} alt="Checkmark icon" />;
    };

    return (
        <div className={`AccordionsWrapper ${styles.AccordionsWrapper}`}>
            {items.map(({ message, condition, status }) => (
                <Accordion
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
    );
};
