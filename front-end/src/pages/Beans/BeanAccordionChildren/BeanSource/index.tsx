import { Tree, type TreeDataNode } from "antd";
import { useTranslation } from "react-i18next";

import { EBeanOrigin, type IBeanSource } from "models";

import sharedStyles from "../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    beanSource: IBeanSource;
}

export const BeanSource = ({ beanSource }: IProps) => {
    const { t } = useTranslation();

    const statelessBeanSource =
        beanSource.origin === EBeanOrigin.UNKNOWN || beanSource.origin === EBeanOrigin.COMPONENT_ANNOTATION;

    const resolveTreeChildrens = (): TreeDataNode[] | undefined => {
        if (beanSource.origin === EBeanOrigin.BEAN_METHOD) {
            return [
                {
                    title: beanSource.origin,
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>Bean Method:</div>
                                    <div className={styles.BeanTreeValue}>{beanSource.methodName}</div>
                                </div>
                            ),
                            key: beanSource.methodName!,
                        },
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>Enclosing class name:</div>
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
                    title: beanSource.origin,
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>Factory Bean Name:</div>
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
            <div className={sharedStyles.AccordionBodyChunkTitle}>{t(`Beans.beanSource`)}:</div>
            {statelessBeanSource ? beanSource.origin : <Tree showLine treeData={resolveTreeChildrens()} />}
        </>
    );
};
