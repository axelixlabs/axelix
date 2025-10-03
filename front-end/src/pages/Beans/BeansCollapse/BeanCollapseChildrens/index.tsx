import { useTranslation } from "react-i18next";
import type { RefObject, SetStateAction, Dispatch } from "react";

import type { IBean, IBeansCollapseHeaderRefs } from "models";

import styles from "./styles.module.css";

interface IProps {
  /**
   * List of beans: full or filtered
   */
  beans: IBean[];
  /**
   * Single bean
   */
  bean: IBean;
  /**
   * Ref to bean collapse headers, used for smooth scrolling.
   */
  headerRefs: RefObject<IBeansCollapseHeaderRefs>;
  /**
   * Setter for the active key in a Collapse component.
   */
  setActiveKey: Dispatch<SetStateAction<string | string[]>>;
}

export const BeanCollapseChildrens = ({
  beans,
  bean,
  headerRefs,
  setActiveKey,
}: IProps) => {
  const { t } = useTranslation();

  const handleDependencyClick = (dependency: string): void => {
    const beanExists = beans.find(({ beanName }) => beanName === dependency);
    if (beanExists) {
      setActiveKey(dependency);

      // Since the scroll does not work correctly due to the specifics of Ant Design's Collapse,
      // a setTimeout with a very short delay is used.
      setTimeout(() => {
        const element = headerRefs.current[dependency];
        if (element) {
          element.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      }, 300);
    }
  };

  return (
    <div className={styles.CollapseBody}>

      <div className={styles.CollapseBodyChunkTitle}>{t("dependencies")}:</div>
      <div>
        {
            bean.dependencies.length > 0
                ? buildDependencies(bean, handleDependencyClick)
                : emptyList()
        }
      </div>

      <div className={styles.CollapseBodyChunkTitle}>{t("aliases")}:</div>
      <div>
        {
            bean.aliases.length > 0
                ? buildAliases(bean)
                : emptyList()
        }
      </div>
    </div>
  );
};

function emptyList() {
    return <div>-</div>;
}

function buildDependencies(
    bean : IBean,
    handleDependencyClick: (dependency: string) => void
) {
    return bean.dependencies.map((dependency) => {
        return <div key={dependency} className={styles.CollapseBodyChunkList}>
            <span
                onClick={() => handleDependencyClick(dependency)}
                className={styles.Dependency}
            >
                {dependency}
            </span>
        </div>
    })
}

function buildAliases(bean: IBean) {
    return bean.aliases.map((aliase) => (
        <div key={aliase} className={styles.CollapseBodyChunkList}>
            {aliase}
        </div>
    ));
}

