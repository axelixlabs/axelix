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
import { useParams } from "react-router-dom";

import { StyledLink } from "components";
import { normalizeHtmlElementId } from "helpers";
import { EBeanOrigin, type IBean } from "models";

import sharedStyles from "../styles.module.css";

import { BeanSourceTree } from "./BeanSourceTree";

interface IProps {
    /**
     * The profile of the given bean
     */
    bean: IBean;
}

export const BeanSource = ({ bean }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const { beanName, beanSource, autoConfigurationRef, isConfigPropsBean } = bean;
    const { origin } = beanSource;

    const statelessBeanSource =
        origin === EBeanOrigin.UNKNOWN ||
        origin === EBeanOrigin.COMPONENT_ANNOTATION ||
        origin === EBeanOrigin.SYNTHETIC_BEAN;

    let beanSourceTitle;

    if (origin === EBeanOrigin.COMPONENT_ANNOTATION && autoConfigurationRef) {
        beanSourceTitle = (
            <StyledLink href={`/instance/${instanceId}/conditions#${normalizeHtmlElementId(autoConfigurationRef)}`}>
                {t("Beans.beanSource.AUTO_CONFIGURATION_CLASS")}
            </StyledLink>
        );
    } else if (origin == EBeanOrigin.UNKNOWN && isConfigPropsBean) {
        beanSourceTitle = (
            <StyledLink href={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(beanName)}`}>
                {t(`Beans.beanSource.CONFIG_PROPS_BEAN`)}
            </StyledLink>
        );
    } else {
        beanSourceTitle = t(`Beans.beanSource.${origin}`);
    }

    return (
        <>
            <div className={sharedStyles.AccordionBodyChunkTitle}>{t(`Beans.beanSource.tree.main`)}:</div>

            {statelessBeanSource ? beanSourceTitle : <BeanSourceTree bean={bean} />}
        </>
    );
};
