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
import { Accordion } from "@/components";
import { splitProperties } from "@/helpers";
import type { IEnvironmentPropertySource, TPrecedenceIndex } from "@/models";

import { EnvironmentPropertiesAccordionBody } from "./EnvironmentPropertiesAccordionBody";
import { EnvironmentPropertiesAccordionHeader } from "./EnvironmentPropertiesAccordionHeader";
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
    const { properties } = propertySource;
    const [withDropDown, withoutDropDown] = splitProperties(properties, precedenceIndex);

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
                    <EnvironmentPropertiesAccordionHeader
                        properties={propertySource.properties}
                        propertySource={propertySource}
                    />
                }
                wrapperStyles={styles.PanelAccordion}
                headerStyles={styles.PanelHeader}
                contentStyles={styles.PanelBody}
                accordionExpanded
            >
                <EnvironmentPropertiesAccordionBody allProperties={allProperties} precedenceIndex={precedenceIndex} />
            </Accordion>
        </div>
    );
};
