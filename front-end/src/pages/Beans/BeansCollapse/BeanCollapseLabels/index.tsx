import type { JSX, RefObject } from "react";
import type { IBean, IBeansCollapseHeaderRefs } from "models";

import { TooltipWithCopy } from "components";

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
      ref={(el) => {
        headerRefs.current[beanName] = el;
      }}
      className={styles.CollapseHeader}
    >
      <div>
        <TooltipWithCopy text={beanName} />
        <div className={styles.ClassName}>
          <TooltipWithCopy text={className} />
        </div>
      </div>
      <div className={styles.ScopeWrapper}>{scope}</div>
    </div>
  );
};
