import styles from '../styles.module.css'
import {EConditionStatus, type ICondition, type IConditionBeanNegative} from "models";

import { Copy } from "components";
import { ConditionsAccordionEntry } from "../ConditionAccordionEntry";
import { Fragment } from 'react/jsx-runtime';

interface IProps {
    /**
     * Negative or positive match
     */
    negativeMatches: IConditionBeanNegative[]
}

export const NegativeMatchTarget = ({ negativeMatches }: IProps) => {
    return (
        <>
            {negativeMatches.map(({ target, matched, notMatched }) => {
                const itemsWithStatus = [
                    ...notMatched.map<ICondition>(item => ({ ...item, status: EConditionStatus.NOT_MATCHED })),
                    ...matched.map<ICondition>(item => ({ ...item, status: EConditionStatus.MATCHED }))
                ];

                return (
                    <Fragment key={target}>
                        <div className={styles.TargetWrapper}>
                            <div>{target}</div>
                            <Copy text={target} />
                        </div>
                        <ConditionsAccordionEntry items={itemsWithStatus} />
                    </Fragment>
                )
            })}
        </>

    );
}