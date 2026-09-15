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

import { Accordion } from "@/components";
import { precedenceChainOf } from "@/helpers";
import type { IEnvProperty, TPrecedenceIndex } from "@/models";

import { EnvironmentPropertyDetails } from "../../EnvironmentPropertyDetails";
import { EnvironmentProperty } from "../EnvironmentProperty";

import styles from "./styles.module.css";

interface IProps {
    allProperties: {
        property: IEnvProperty;
        hasDropdown: boolean;
    }[];
    precedenceIndex: TPrecedenceIndex;
}

export const EnvironmentPropertiesAccordionBody = ({ allProperties, precedenceIndex }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
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
        </>
    );
};
