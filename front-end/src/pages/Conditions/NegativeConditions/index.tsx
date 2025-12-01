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
            {negativeMatches.map(({ className, methodName, matched, notMatched }) => {
                const itemsWithStatus = [
                    ...notMatched.map((item) => ({ ...item, status: EConditionStatus.NOT_MATCHED })),
                    ...matched.map((item) => ({ ...item, status: EConditionStatus.MATCHED })),
                ];

                return (
                    <Fragment key={className + methodName}>
                        <div className={styles.ConditionHeaderWrapper}>
                            <div className={styles.ConditionHeaderSection}>
                                <span style={{ fontWeight: 300 }}>Class:</span> {className}
                                <Copy text={className} />
                            </div>
                            {methodName && (
                                <>
                                    <div className={styles.ConditionHeaderSection}>
                                        <span style={{ fontWeight: 300 }}>Method:</span> {methodName}
                                        <Copy text={className} />
                                    </div>
                                </>
                            )}
                        </div>
                        <ConditionsAccordionEntry items={itemsWithStatus} />
                    </Fragment>
                );
            })}
        </>
    );
};
