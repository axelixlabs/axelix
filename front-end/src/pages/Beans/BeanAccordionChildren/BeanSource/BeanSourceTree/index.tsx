/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { Tree, type TreeDataNode } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { StyledLink } from "components";
import { normalizeHtmlElementId } from "helpers";
import { EBeanOrigin, ESearchSubject, type IBean } from "models";
import { scrollToAccordionById } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The data of a single bean
     */
    bean: IBean;
}

export const BeanSourceTree = ({ bean }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { beanName, beanSource, autoConfigurationRef, isConfigPropsBean } = bean;

    const resolveTreeChildren = (): TreeDataNode[] | undefined => {
        if (beanSource.origin === EBeanOrigin.BEAN_METHOD) {
            return [
                {
                    title: autoConfigurationRef
                        ? t("Beans.beanSource.AUTO_CONFIGURATION_BEAN_METHOD")
                        : t("Beans.beanSource.BEAN_METHOD"),
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>{t("Beans.beanSource.tree.beanMethod")}:</div>
                                    <div className={styles.BeanTreeValue}>{beanSource.methodName}</div>
                                </div>
                            ),
                            selectable: false,
                            key: beanSource.methodName!,
                        },
                        {
                            title: (
                                <div
                                    className={`${styles.ClickableBeanTreeItem}`}
                                    onClick={() =>
                                        scrollToAccordionById(
                                            beanSource.enclosingClassFullName!,
                                            ESearchSubject.BEAN_CLASS,
                                        )
                                    }
                                >
                                    <div className={styles.BeanTreeLabel}>
                                        {autoConfigurationRef
                                            ? t("Beans.beanSource.tree.autoConfigurationEnclosingClass")
                                            : t("Beans.beanSource.tree.enclosingClass")}
                                        :
                                    </div>
                                    <div className={styles.BeanTreeValue}>{beanSource.enclosingClassName}</div>
                                </div>
                            ),
                            key: beanSource.enclosingClassName!,
                        },

                        // TODO: Maybe we can refactor this in the future
                        ...(autoConfigurationRef
                            ? [
                                  {
                                      title: (
                                          <StyledLink
                                              href={`/instance/${instanceId}/conditions#${normalizeHtmlElementId(autoConfigurationRef)}`}
                                          >
                                              {t("Beans.beanSource.tree.conditionsPageRef")}
                                          </StyledLink>
                                      ),
                                      key: autoConfigurationRef,
                                  },
                              ]
                            : []),
                        ...(isConfigPropsBean
                            ? [
                                  {
                                      title: (
                                          <StyledLink
                                              href={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(beanName)}`}
                                          >
                                              {t("Beans.beanSource.tree.configPropsBean")}
                                          </StyledLink>
                                      ),
                                      key: "isConfigPropsBean",
                                  },
                              ]
                            : []),
                    ],
                },
            ];
        }

        if (beanSource.origin === EBeanOrigin.FACTORY_BEAN) {
            return [
                {
                    title: t(`Beans.beanSource.${EBeanOrigin.FACTORY_BEAN}`),
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>
                                        {t("Beans.beanSource.tree.factoryBeanClassName")}:
                                    </div>
                                    <div className={styles.BeanTreeValue}>{beanSource.factoryBeanName}</div>
                                </div>
                            ),
                            selectable: false,
                            key: beanSource.factoryBeanName!,
                        },
                    ],
                },
            ];
        }
    };

    return (
        <>
            <Tree expandAction="click" showLine treeData={resolveTreeChildren()} className={styles.Tree} />
        </>
    );
};
