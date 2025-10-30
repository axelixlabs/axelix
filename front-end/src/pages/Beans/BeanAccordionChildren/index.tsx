import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { TooltipWithCopy } from "components";
import { type IBean } from "models";

import { BeanBooleanFlag } from "./BeanBooleanFlag";
import { BeanProxyType } from "./BeanProxyType";
import { BeanSimpleList } from "./BeanSimpleList";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
    /**
     * Setter for the state that indicates whether the selected dependency equals the bean name
     */
    setActiveKey: Dispatch<SetStateAction<string>>;
}

export const BeanAccordionChildren = ({ bean, setActiveKey }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.AccordionBody}>
            <div className={styles.AccordionBodyChunkTitle}>{t("Beans.dependencies")}:</div>
            <div>
                {!bean.dependencies.length ? (
                    <span>-</span>
                ) : (
                    bean.dependencies.map((dependency) => (
                        <div
                            key={dependency}
                            className={styles.AccordionBodyChunkList}
                            onClick={() => setActiveKey(dependency)}
                        >
                            {/* TODO: This part we need to be fix after tooltip PR merge */}
                            <a href={`#${dependency}`} className={styles.Dependency}>
                                <TooltipWithCopy text={dependency} />
                            </a>
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
