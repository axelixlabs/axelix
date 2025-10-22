import styles from '../styles.module.css'
import type { IConditionBeanPositive } from "models";

import { Copy } from "components";
import { ConditionsAccordionEntry } from '../ConditionAccordionEntry';
import { Fragment } from 'react/jsx-runtime';

interface IProps {
  /**
   * Negative or positive match
   */
  positiveMatches: IConditionBeanPositive[]
}

export const PositiveMatchTarget = ({ positiveMatches }: IProps) => {
  return (
    <>
      {positiveMatches.map(({ target, matched }) => (
        <Fragment key={target}>
          <div className={styles.TargetWrapper}>
            <div>{target}</div>
            <Copy text={target} />
          </div>
          <ConditionsAccordionEntry items={matched} />
        </Fragment>
      ))}
    </>
  );
}