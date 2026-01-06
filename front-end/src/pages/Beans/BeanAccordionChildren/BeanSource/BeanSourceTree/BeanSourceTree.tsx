/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Tree, type TreeDataNode } from "antd";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { normalizeHtmlElementId } from "helpers";
import { EBeanOrigin, ESearchSubject, type IBean } from "models";
import { scrollToAccordionById } from "utils";

import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * The data of a single bean
     */
    bean: IBean;

    /**
     * The translated title of a bean source
     */
    translatedTitle: string;
}

export const BeanSourceTree = ({ bean, translatedTitle }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { beanSource, autoConfigurationRef } = bean;

    const resolveTreeChildren = (): TreeDataNode[] | undefined => {
        if (beanSource.origin === EBeanOrigin.BEAN_METHOD) {
            return [
                {
                    title: `${autoConfigurationRef ? "Autoconfiguration " : ""} ${translatedTitle}`,
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
                                        {t("Beans.beanSource.titles.enclosingClass")}:
                                    </div>
                                    <div className={styles.BeanTreeValue}>{beanSource.enclosingClassName}</div>
                                </div>
                            ),
                            key: beanSource.enclosingClassName!,
                        },
                        ...(autoConfigurationRef
                            ? [
                                  {
                                      title: (
                                          <div className={styles.BeanTreeItem}>
                                              <div className={styles.BeanTreeLabel}>Autoconfiguration ref</div>
                                              <Link
                                                  to={`/instance/${instanceId}/conditions#${normalizeHtmlElementId(autoConfigurationRef)}`}
                                                  onClick={(e) => e.stopPropagation()}
                                              >
                                                  <img src={LinkIcon} alt="Link icon" />
                                              </Link>
                                          </div>
                                      ),
                                      key: autoConfigurationRef,
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
                    title: translatedTitle,
                    key: beanSource.origin,
                    children: [
                        {
                            title: (
                                <div className={styles.BeanTreeItem}>
                                    <div className={styles.BeanTreeLabel}>
                                        {t("Beans.beanSource.titles.factoryBeanClassName")}:
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

    return <Tree expandAction="click" showLine treeData={resolveTreeChildren()} className={styles.Tree} />;
};
