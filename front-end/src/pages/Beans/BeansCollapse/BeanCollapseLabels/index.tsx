import type { JSX, RefObject } from "react";
import type { IBean, IBeansCollapseHeaderRefs } from "models";

import styles from "./styles.module.css";

interface IProps {
  /**
   * Single bean
   */
  bean: IBean;
  /**
   * Ref to bean collapse headers, used for smooth scrolling.
   */
  headerRefs: RefObject<IBeansCollapseHeaderRefs>;
}

export const BeanCollapseLabels = ({
  bean,
  headerRefs,
}: IProps): JSX.Element => {
  const { beanName, className, scope } = bean;

  return (
    <div
      className={styles.CollapseHeader}
      ref={(el) => {
        headerRefs.current[beanName] = el;
      }}
    >
      <div>
        <p>{beanName}</p>
        <p className={styles.ClassName}>{className}</p>
      </div>
      <div className={styles.ScopeWrapper}>{scope}</div>
    </div>
  );
};
