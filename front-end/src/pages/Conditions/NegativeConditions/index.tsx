import { Fragment } from "react/jsx-runtime";

import { Copy } from "components";
import { EConditionStatus, type IConditionBeanNegative } from "models";

import { ConditionsAccordionEntry } from "../ConditionAccordionEntry";
import styles from "../styles.module.css";

interface IProps {
    /**
     * Negative or positive match
     */
    negativeMatches: IConditionBeanNegative[];
}

export const NegativeConditions = ({ negativeMatches }: IProps) => {
    return (
        <>
            {negativeMatches.map(({ target, matched, notMatched }) => {
                const itemsWithStatus = [
                    ...notMatched.map((item) => ({ ...item, status: EConditionStatus.NOT_MATCHED })),
                    ...matched.map((item) => ({ ...item, status: EConditionStatus.MATCHED })),
                ];

                return (
                    <Fragment key={target}>
                        <div className={styles.TargetWrapper}>
                            <div>{target}</div>
                            <Copy text={target} />
                        </div>
                        <ConditionsAccordionEntry items={itemsWithStatus} />
                    </Fragment>
                );
            })}
        </>
    );
};
