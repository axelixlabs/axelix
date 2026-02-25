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
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { StyledLink } from "components";
import { normalizeHtmlElementId, uniqueInjectionPointsBeanNames } from "helpers";
import type { IEnvProperty } from "models";

import styles from "./styles.module.css";

import { LinkIcon } from "assets";

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
        <>
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
                        <StyledLink
                            href={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(configPropsBeanName)}`}
                        >
                            {configPropsBeanName}
                        </StyledLink>
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
                                        <LinkIcon />
                                    </Link>
                                </div>
                            ))}
                        </div>
                    </>
                )}
            </div>
        </>
    );
};
