import { Fragment } from "react/jsx-runtime";

import { Copy } from "components";
import { EConditionStatus, type IConditionBeanPositive } from "models";

import { ConditionsAccordionEntry } from "../ConditionAccordionEntry";
import styles from "../styles.module.css";

interface IProps {
    /**
     * Negative or positive match
     */
    positiveMatches: IConditionBeanPositive[];
}

export const PositiveConditions = ({ positiveMatches }: IProps) => {
    return (
        <>
            {positiveMatches.map(({ className, methodName, matched }) => {
                const items = matched.map((item) => {
                    return {
                        ...item,
                        status: EConditionStatus.MATCHED,
                    };
                });

                return (
                    <Fragment key={className + methodName}>
                        <div className={styles.TargetWrapper}>
                            <div>Class: {className}</div>
                            <Copy text={className} />
                            {methodName && (
                                <>
                                    <div>Method: {methodName}</div>
                                    <Copy text={methodName} />
                                </>
                            )}
                        </div>
                        <ConditionsAccordionEntry items={items} />
                    </Fragment>
                );
            })}
        </>
    );
};
