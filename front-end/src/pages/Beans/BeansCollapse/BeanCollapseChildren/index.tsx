import type { Dispatch, RefObject, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { TooltipWithCopy } from "components";
import { type IBean, type IBeansCollapseHeaderRefs } from "models";

import { BeanBooleanFlag } from "./BeanBooleanFlag";
import { BeanProxyType } from "./BeanProxyType";
import { BeanSimpleList } from "./BeanSimpleList";
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

export const BeanCollapseChildren = ({ beans, bean, headerRefs, setActiveKey }: IProps) => {
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
            <div className={styles.CollapseBodyChunkTitle}>{t("Beans.dependencies")}:</div>
            <div>
                {!bean.dependencies.length ? (
                    <span>-</span>
                ) : (
                    bean.dependencies.map((dependency) => (
                        <div key={dependency} className={styles.CollapseBodyChunkList}>
                            <div className={styles.Dependency}>
                                <TooltipWithCopy text={dependency} onClick={() => handleDependencyClick(dependency)} />
                            </div>
                        </div>
                    ))
                )}
            </div>

            <BeanSimpleList valuesTag="aliases" values={bean.aliases}></BeanSimpleList>
            <BeanSimpleList valuesTag="qualifiers" values={bean.qualifiers}></BeanSimpleList>
            <BeanProxyType proxyType={bean.proxyType} />
            <BeanBooleanFlag valueTag={"isLazyInitBean"} value={bean.isLazyInit} />
            <BeanBooleanFlag valueTag={"isPrimaryBean"} value={bean.isPrimary} />
        </div>
    );
};
