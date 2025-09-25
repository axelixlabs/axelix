import type { IBean } from "models";
import { useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Collapse, type CollapseProps } from "antd";

import styles from "./styles.module.css";

interface IProps {
  /**
   * List of beans: full or filtered
   */
  beans: IBean[];
}

export const BeansCollapse = ({ beans }: IProps) => {
  const { t } = useTranslation();

  const [activeKey, setActiveKey] = useState<string | string[]>([]);

  const headerRefs = useRef<{ [key: string]: HTMLDivElement | null }>({});

  const createCollapseItems = (items: IBean[]): CollapseProps["items"] => {
    return items.map(
      ({ beanName, className, scope, dependencies, aliases }) => ({
        key: beanName,
        label: (
          <div
            className={styles.CollapseHeader}
            ref={(el) => {
              headerRefs.current[beanName] = el;
            }}
          >
            <div>
              <p>{beanName}</p>
              <p>{className}</p>
            </div>
            <div className={styles.ScopeWrapper}>{scope}</div>
          </div>
        ),
        children: (
          <div className={styles.CollapseBody}>
            <div className={styles.CollapseBodyChunkTitle}>
              {t("dependencies")}:
            </div>
            <div>
              {dependencies.map((dependency) => (
                <div key={dependency} className={styles.CollapseBodyChunkList}>
                  <span
                    onClick={() => handleDependencyClick(dependency)}
                    className={styles.Dependency}
                  >
                    {dependency}
                  </span>
                </div>
              ))}
            </div>
            <div className={styles.CollapseBodyChunkTitle}>{t("aliases")}:</div>
            <div>
              {aliases.map((aliase) => (
                <div key={aliase} className={styles.CollapseBodyChunkList}>
                  {aliase}
                </div>
              ))}
            </div>
          </div>
        ),
      })
    );
  };

  const handleDependencyClick = (dependency: string): void => {
    const beanExists = beans.find(({ beanName }) => beanName === dependency);
    if (beanExists) {
      setActiveKey(dependency);

      // Since the scroll does not work correctly due to the specifics of Ant Design's Collapse, a setTimeout with a very short delay is used.
      setTimeout(() => {
        const element = headerRefs.current[dependency];
        if (element) {
          element.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      }, 300);
    }
  };

  return (
    <Collapse
      accordion
      activeKey={activeKey}
      items={createCollapseItems(beans)}
      onChange={(key) => setActiveKey(key)}
    />
  );
};
