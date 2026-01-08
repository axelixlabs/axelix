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
import InfoIcon from "assets/icons/info.svg?react";

import { Accordion, Copy, EmptyHandler, InfoTooltip } from "components";
import { splitProperties } from "helpers";
import type { IEnvironmentPropertySource } from "models";

import { EnvironmentAccordionBody } from "../EnvironmentAccordionBody";
import { EnvironmentAccordionHeader } from "../EnvironmentAccordionHeader";
import { EnvironmentPropertyValue } from "../EnvironmentPropertyValue";

import styles from "./styles.module.css";

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
                                    headerStyles={property.deprecation ? styles.DeprecatedPropertyAccordionsHeader : ""}
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
    );
};
