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
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { normalizeHtmlElementId } from "helpers";
import { ESearchSubject, type IBean } from "models";
import { scrollToAccordionById } from "utils";

import { BeanBooleanFlag } from "./BeanBooleanFlag";
import { BeanProxyType } from "./BeanProxyType";
import { BeanSimpleList } from "./BeanSimpleList";
import { BeanSource } from "./BeanSource";
import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
}

export const BeanAccordionChildren = ({ bean }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    return (
        <div className={styles.AccordionBody}>
            <div className={styles.AccordionBodyChunkTitle}>{t("Beans.dependencies")}:</div>
            <div>
                {!bean.dependencies.length ? (
                    <span>-</span>
                ) : (
                    bean.dependencies.map(({ name, isConfigPropsDependency }) => (
                        <div key={name} className={styles.AccordionBodyChunkList}>
                            <div className={styles.DependencyWrapper}>
                                <div
                                    className={styles.Dependency}
                                    onClick={() => scrollToAccordionById(name, ESearchSubject.BEAN_NAME_OR_ALIAS)}
                                >
                                    <TooltipWithCopy text={name} />
                                </div>
                                {isConfigPropsDependency && (
                                    <Link to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(name)}`}>
                                        <img src={LinkIcon} alt="Link icon" />
                                    </Link>
                                )}
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
            <BeanSource bean={bean} />
        </div>
    );
};
