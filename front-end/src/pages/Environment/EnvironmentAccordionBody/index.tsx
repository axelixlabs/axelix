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

import { normalizeHtmlElementId, uniqueInjectionPointsBeanNames } from "helpers";
import type { IEnvProperty } from "models";

import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;
}

export const EnvironmentAccordionBody = ({ property }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const { deprecation, description, configPropsBeanName, injectionPoints } = property;

    return (
        <div className={styles.AccordionBody}>
            {deprecation && (
                <>
                    <div>{t(`Environments.deprecated`)}:</div>
                    <div className={styles.Value}>{deprecation.message}</div>
                </>
            )}

            {description && (
                <>
                    <div>{t(`Environments.description`)}:</div>
                    <div className={styles.Value}>{description}</div>
                </>
            )}

            {configPropsBeanName && (
                <>
                    <div>{t(`Environments.configProps`)}:</div>
                    <div className={styles.Value}>
                        {configPropsBeanName}
                        <Link
                            to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(configPropsBeanName)}`}
                            className={styles.LinkIcon}
                        >
                            <img src={LinkIcon} alt="Link icon" />
                        </Link>
                    </div>
                </>
            )}

            {injectionPoints && (
                <>
                    <div>{t(`Environments.injectedIn`)}:</div>
                    <div>
                        {uniqueInjectionPointsBeanNames(injectionPoints).map((beanName) => (
                            <div className={`${styles.Value} ${styles.InjectionPointWrapper}`} key={beanName}>
                                {beanName}
                                <Link
                                    to={`/instance/${instanceId}/beans#${normalizeHtmlElementId(beanName)}`}
                                    className={styles.LinkIcon}
                                >
                                    <img src={LinkIcon} alt="Link icon" />
                                </Link>
                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};
