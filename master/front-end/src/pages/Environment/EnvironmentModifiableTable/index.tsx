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

import { InfoIcon } from "@/assets";
import { Accordion, HintTooltip } from "@/components";
import { precedenceChainOf, splitProperties } from "@/helpers";
import type { IEnvironmentPropertySource, TPrecedenceIndex } from "@/models";

import { EnvironmentPropertyDetails } from "../EnvironmentPropertyDetails";

import { EnvironmentProperty } from "./EnvironmentProperty";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The property source data
     */
    propertySource: IEnvironmentPropertySource;

    /**
     * Index of every property occurrence across all the property sources, used to render the
     * precedence chain of an individual property
     */
    precedenceIndex: TPrecedenceIndex;
}

export const EnvironmentModifiableTable = ({ propertySource, precedenceIndex }: IProps) => {
    const { t } = useTranslation();

    const { name, properties, description } = propertySource;
    const [withDropDown, withoutDropDown] = splitProperties(properties, precedenceIndex);

    const deprecatedCount = properties.filter(({ deprecation }) => deprecation).length;

    const allProperties = [
        ...withDropDown.map((property) => ({
            property,
            hasDropdown: true,
        })),
        ...withoutDropDown.map((property) => ({
            property,
            hasDropdown: false,
        })),
    ];

    return (
        <div className={styles.Panel}>
            <Accordion
                header={
                    <div className={styles.PanelHeaderInner}>
                        <span className={styles.PanelTitleGroup}>
                            <span className={styles.PanelTitle}>{name}</span>
                            {description && (
                                <HintTooltip
                                    placement="bottomLeft"
                                    content={
                                        <>
                                            <span className={styles.TooltipName}>{name}</span>
                                            <span>{description}</span>
                                        </>
                                    }
                                >
                                    <span className={styles.InfoTrigger}>
                                        <InfoIcon color="currentColor" />
                                    </span>
                                </HintTooltip>
                            )}
                        </span>

                        <span className={styles.PanelCounters}>
                            {deprecatedCount > 0 && (
                                <span className={styles.FlaggedCounter}>
                                    {t("Environments.flaggedCount", { value: deprecatedCount })}
                                </span>
                            )}
                            <span className={styles.PropertiesCounter}>
                                {t("Environments.propertiesCount", { value: properties.length })}
                            </span>
                        </span>
                    </div>
                }
                wrapperStyles={styles.PanelAccordion}
                headerStyles={styles.PanelHeader}
                contentStyles={styles.PanelBody}
                accordionExpanded
            >
                {allProperties.length === 0 ? (
                    <div className={styles.EmptySource}>{t("Environments.noPropertiesInSource")}</div>
                ) : (
                    allProperties.map(({ property, hasDropdown }) => {
                        if (hasDropdown) {
                            return (
                                <Accordion
                                    header={<EnvironmentProperty property={property} />}
                                    wrapperStyles={styles.RowAccordion}
                                    headerStyles={styles.RowHeader}
                                    contentStyles={styles.RowBody}
                                    key={property.name}
                                >
                                    <EnvironmentPropertyDetails
                                        property={property}
                                        precedenceChain={precedenceChainOf(precedenceIndex, property.name)}
                                    />
                                </Accordion>
                            );
                        }

                        return <EnvironmentProperty property={property} caretPlaceholder key={property.name} />;
                    })
                )}
            </Accordion>
        </div>
    );
};
