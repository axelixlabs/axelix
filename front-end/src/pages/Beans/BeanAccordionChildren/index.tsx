import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { TooltipWithCopy } from "components";
import { type IBean } from "models";

import { BeanBooleanFlag } from "./BeanBooleanFlag";
import { BeanProxyType } from "./BeanProxyType";
import { BeanSimpleList } from "./BeanSimpleList";
import { BeanSource } from "./BeanSource";
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
                    bean.dependencies.map(({ name }) => (
                        <div key={name} className={styles.AccordionBodyChunkList} onClick={() => setActiveKey(name)}>
                            {/* TODO: This part we need to be fix after tooltip PR merge */}
                            <a href={`#${name}`} className={styles.Dependency}>
                                <TooltipWithCopy text={name} />
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
            <BeanSource beanSource={bean.beanSource} />
        </div>
    );
};
