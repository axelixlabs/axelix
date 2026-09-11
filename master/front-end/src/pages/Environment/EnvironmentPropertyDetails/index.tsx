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
import type { PropsWithChildren } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router";

import { LinkIcon } from "@/assets";
import { StyledLink } from "@/components";
import { normalizeHtmlElementId, uniqueInjectionPointsBeanNames } from "@/helpers";
import type { IEnvProperty, IPropertyOccurrence } from "@/models";

import { EnvironmentPrecedenceChain } from "../EnvironmentPrecedenceChain";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;

    /**
     * Every occurrence of this property across all the property sources, highest precedence first
     */
    precedenceChain: IPropertyOccurrence[];
}

const DetailRow = ({ label, children }: PropsWithChildren<{ label: string }>) => (
    <div className={styles.DetailRow}>
        <span className={styles.DetailLabel}>{label}</span>
        <div className={styles.DetailValue}>{children}</div>
    </div>
);

export const EnvironmentPropertyDetails = ({ property, precedenceChain }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const { deprecation, description, configPropsBeanName, injectionPoints } = property;

    return (
        <div className={styles.AccordionBody}>
            {deprecation && <DetailRow label={t("Environments.deprecated")}>{deprecation.message}</DetailRow>}

            {description && <DetailRow label={t("Environments.description")}>{description}</DetailRow>}

            {configPropsBeanName && (
                <DetailRow label={t("Environments.configProps")}>
                    <StyledLink
                        href={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(configPropsBeanName)}`}
                    >
                        <span className={styles.MonoValue}>{configPropsBeanName}</span>
                    </StyledLink>
                </DetailRow>
            )}

            {injectionPoints && (
                <DetailRow label={t("Environments.injectedIn")}>
                    {uniqueInjectionPointsBeanNames(injectionPoints).map((beanName) => (
                        <div className={styles.InjectionPointWrapper} key={beanName}>
                            <span className={styles.MonoValue}>{beanName}</span>
                            <Link
                                to={`/instance/${instanceId}/beans#${normalizeHtmlElementId(beanName)}`}
                                className={styles.LinkIcon}
                            >
                                <LinkIcon />
                            </Link>
                        </div>
                    ))}
                </DetailRow>
            )}

            {precedenceChain.length > 1 && (
                <DetailRow label={t("Environments.definedInSources", { value: precedenceChain.length })}>
                    <EnvironmentPrecedenceChain chain={precedenceChain} />
                </DetailRow>
            )}
        </div>
    );
};
