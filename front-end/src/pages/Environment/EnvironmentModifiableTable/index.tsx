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
import { Accordion, Copy, EmptyHandler, InfoTooltip } from "components";
import { splitProperties } from "helpers";
import type { IEnvironmentPropertySource } from "models";

import { EnvironmentAccordionBody } from "../EnvironmentAccordionBody";
import { EnvironmentAccordionHeader } from "../EnvironmentAccordionHeader";
import { EnvironmentPropertyValue } from "../EnvironmentPropertyValue";

import styles from "./styles.module.css";

import { InfoIcon } from "assets";

interface IProps {
    /**
     * The property source data
     */
    propertySource: IEnvironmentPropertySource;
}

export const EnvironmentModifiableTable = ({ propertySource }: IProps) => {
    const { name, properties, description } = propertySource;
    const [withDropDown, withoutDropDown] = splitProperties(properties);

    return (
        <>
            <div className={`AccordionsWrapper ${styles.AccordionWrapper}`}>
                <Accordion
                    header={
                        <div className={styles.AccordionHeader}>
                            {name}
                            {description && (
                                <InfoTooltip text={description}>
                                    <InfoIcon color="#1890ff" />
                                </InfoTooltip>
                            )}
                        </div>
                    }
                    headerStyles={styles.MainAccordionHeaderStyles}
                    accordionExpanded
                >
                    <div className="AccordionsWrapper">
                        <EmptyHandler isEmpty={!properties.length}>
                            {[
                                ...withDropDown.map((property) => (
                                    <Accordion
                                        header={<EnvironmentAccordionHeader property={property} />}
                                        headerStyles={
                                            property.deprecation ? styles.DeprecatedPropertyAccordionsHeader : ""
                                        }
                                        key={property.name}
                                    >
                                        <EnvironmentAccordionBody property={property} />
                                    </Accordion>
                                )),
                                ...withoutDropDown.map((property) => (
                                    <div className={styles.CommonPropertyWrapper} key={property.name}>
                                        <div className={styles.KeyChunk}>
                                            {property.name} <Copy text={property.name} />
                                        </div>
                                        <div className={styles.ValueChunk}>
                                            <EnvironmentPropertyValue property={property} />
                                        </div>
                                    </div>
                                )),
                            ]}
                        </EmptyHandler>
                    </div>
                </Accordion>
            </div>
        </>
    );
};
