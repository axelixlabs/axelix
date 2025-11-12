import { Tree, type TreeDataNode } from "antd";
import { useTranslation } from "react-i18next";

import { EBeanOrigin, type IBeanSource } from "models";
import { ESearchSubject, scrollToAccordionById } from "utils";

import sharedStyles from "../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    beanSource: IBeanSource;
}

export const BeanSource = ({ beanSource }: IProps) => {
    const { t } = useTranslation();

    const statelessBeanSource =
        beanSource.origin === EBeanOrigin.UNKNOWN || beanSource.origin === EBeanOrigin.COMPONENT_ANNOTATION;

    const translatedTitle = t(`Beans.beanSource.${beanSource.origin}`);

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
                                        scrollToAccordionById(beanSource.enclosingClassName!, ESearchSubject.BEAN_CLASS)
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
