import { Tree, type TreeDataNode } from "antd";
import { useTranslation } from "react-i18next";

import { EBeanOrigin, ESearchSubject, type IBean } from "models";
import { scrollToAccordionById } from "utils";

import sharedStyles from "../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The profile of the given bean
     */
    bean: IBean;
}

export const BeanSource = ({ bean }: IProps) => {
    const { t } = useTranslation();

    const beanSource = bean.beanSource;

    const statelessBeanSource =
        beanSource.origin === EBeanOrigin.UNKNOWN ||
        beanSource.origin === EBeanOrigin.COMPONENT_ANNOTATION ||
        beanSource.origin === EBeanOrigin.SYNTHETIC_BEAN;

    let translatedTitle;

    if (beanSource.origin == EBeanOrigin.UNKNOWN && bean.isConfigPropsBean) {
        translatedTitle = t(`Beans.beanSource.CONFIG_PROPS_BEAN`);
    } else {
        translatedTitle = t(`Beans.beanSource.${beanSource.origin}`);
    }

    const resolveTreeChildren = (): TreeDataNode[] | undefined => {
        if (beanSource.origin === EBeanOrigin.BEAN_METHOD) {
            return [
                {
                    title: translatedTitle,
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>
                                        {t("Beans.beanSource.titles.beanMethod")}:
                                    </div>
                                    <div className={styles.BeanTreeValue}>{beanSource.methodName}</div>
                                </div>
                            ),
                            key: beanSource.methodName!,
                        },
                        {
                            title: (
                                <div
                                    className={`${styles.BeanTreeItem} ${styles.BeanTreeItemHover}`}
                                    onClick={() =>
                                        scrollToAccordionById(
                                            beanSource.enclosingClassFullName!,
                                            ESearchSubject.BEAN_CLASS,
                                        )
                                    }
                                >
                                    <div className={styles.BeanTreeLabel}>
                                        {t("Beans.beanSource.titles.enclosingClass")}:
                                    </div>
                                    <div className={styles.BeanTreeValue}>{beanSource.enclosingClassName}</div>
                                </div>
                            ),
                            key: beanSource.enclosingClassName!,
                        },
                    ],
                },
            ];
        }

        if (beanSource.origin === EBeanOrigin.FACTORY_BEAN) {
            return [
                {
                    title: translatedTitle,
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div
                                    className={`${styles.BeanTreeItem} ${styles.BeanTreeItemHover}`}
                                    onClick={() =>
                                        scrollToAccordionById(
                                            beanSource.factoryBeanName!,
                                            ESearchSubject.BEAN_NAME_OR_ALIAS,
                                        )
                                    }
                                >
                                    <div className={styles.BeanTreeLabel}>
                                        {t("Beans.beanSource.titles.factoryBeanName")}:
                                    </div>
                                    <div className={styles.BeanTreeValue}>{beanSource.factoryBeanName}</div>
                                </div>
                            ),
                            key: beanSource.factoryBeanName!,
                        },
                    ],
                },
            ];
        }
    };

    return (
        <>
            <div className={sharedStyles.AccordionBodyChunkTitle}>{t(`Beans.beanSource.titles.main`)}:</div>
            {statelessBeanSource ? (
                translatedTitle
            ) : (
                <Tree expandAction="click" showLine treeData={resolveTreeChildren()} className={styles.Tree} />
            )}
        </>
    );
};
