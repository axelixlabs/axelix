import styles from '../styles.module.css'
import {EConditionStatus, type IConditionBeanPositive} from "models";

import { Copy } from "components";
import { ConditionsAccordionEntry } from '../ConditionAccordionEntry';
import { Fragment } from 'react/jsx-runtime';

interface IProps {
  /**
   * Negative or positive match
   */
  positiveMatches: IConditionBeanPositive[]
}

export const PositiveConditions = ({ positiveMatches }: IProps) => {
  return (
    <>
      {positiveMatches.map(({ target, matched }) => {
        const items = matched.map((item) => {
          return {
            ...item,
            status: EConditionStatus.MATCHED
          }
        });

        return (
          <Fragment key={target}>
            <div className={styles.TargetWrapper}>
              <div>{target}</div>
              <Copy text={target}/>
            </div>
            <ConditionsAccordionEntry items={items}/>
          </Fragment>
        );
      })}
    </>
  );
}