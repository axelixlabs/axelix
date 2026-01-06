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
import { BeanSourceTree } from "./BeanSourceTree/BeanSourceTree";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { getBeanSourceTranslatedTitle, normalizeHtmlElementId } from "helpers";
import { EBeanOrigin, type IBean } from "models";

import sharedStyles from "../styles.module.css";

import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * The profile of the given bean
     */
    bean: IBean;
}

export const BeanSource = ({ bean }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const { beanSource, isConfigPropsBean, autoConfigurationRef } = bean;

    const statelessBeanSource =
        beanSource.origin === EBeanOrigin.UNKNOWN ||
        beanSource.origin === EBeanOrigin.COMPONENT_ANNOTATION ||
        beanSource.origin === EBeanOrigin.SYNTHETIC_BEAN;

    const translatedTitle = getBeanSourceTranslatedTitle(beanSource, isConfigPropsBean, t);

    return (
        <>
            <div className={sharedStyles.AccordionBodyChunkTitle}>{t(`Beans.beanSource.titles.main`)}:</div>

            {beanSource.origin === EBeanOrigin.COMPONENT_ANNOTATION && autoConfigurationRef ? (
                <div className={styles.LinkedTitleWrapper}>
                    <div>Autoconfiguration {translatedTitle}</div>
                    <Link to={`/instance/${instanceId}/conditions#${normalizeHtmlElementId(autoConfigurationRef)}`}>
                        <img src={LinkIcon} alt="Link icon" />
                    </Link>
                </div>
            ) : statelessBeanSource ? (
                translatedTitle
            ) : (
                <BeanSourceTree bean={bean} translatedTitle={translatedTitle} />
            )}
        </>
    );
};
