import {Collapse} from "antd";
import {useState, type ReactNode} from "react";

import styles from './styles.module.css'
import type {ICondition, INegativeMatches, IPositiveMatches} from "models";

import Checkmark from 'assets/icons/checkmark.svg'
import Close from 'assets/icons/close.svg'
import {Copy} from "components";

interface IProps {
  /**
   * Negative or positive match
   */
  match: INegativeMatches | IPositiveMatches
}

export const ConditionsAccordion = ({match}: IProps) => {
  const notMatched = "notMatched" in match ? match.notMatched : undefined;
  const matched = "matched" in match ? match.matched : undefined;
  const matches = "matches" in match ? match.matches : undefined;

  const [activeKey, setActiveKey] = useState<string | string[]>([]);

  // TODO: In the future, provide the return type CollapseProps["items"], or when Collapse is replaced with a custom Collapse, this type will no longer be needed.
  const createCollapseItems = (items: ICondition[], icon?: ReactNode) => (
    items.map(({message, condition}) => ({
      key: `${message} ${condition}`,
      label: (
        <div className={styles.LabelWrapper}>
          {icon} {condition}
        </div>
      ),
      children: <div className={styles.Message}>{message}</div>
    }))
  )

  const getItems = () => {
    if (notMatched && matched) {
      return [
        ...createCollapseItems(notMatched, <img src={Close} alt="Close icon"/>),
        ...createCollapseItems(matched, <img src={Checkmark} alt="Checkmark icon"/>)
      ]
    }

    if (matches) {
      return createCollapseItems(matches)
    }

    return []
  }

  return (
    <>
      <div className={styles.TargetWrapper}>
        <div>{match.target}</div>
        <Copy text={match.target}/>
      </div>

      <Collapse
        accordion
        activeKey={activeKey}
        items={getItems()}
        onChange={(key) => setActiveKey(key)}
        bordered
        className={`Collapse ${styles.Collapse}`}
      />
    </>

  );
}