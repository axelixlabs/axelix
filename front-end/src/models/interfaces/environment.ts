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
import type { ITableRow } from "./globals";

interface IProperties {
    /**
     * The property name
     */
    name: string;
    /**
     * The property value
     */
    value: string;
    /**
     * True if propertyValue is primary, false otherwise
     */
    isPrimary: boolean;
    /**
     * flag that designates that the bean is the config props bean.
     */
    configPropsBeanName: string | null;
}

export interface IEnvironmentPropertySource {
    /**
     * Environment property source name
     */
    name: string;
    /**
     * Environment properties list
     */
    properties: IProperties[];
}

export interface IEnvironmentResponseBody {
    /**
     * Environment active profiles list
     */
    activeProfiles: string[];

    /**
     * Environment default profiles list
     */
    defaultProfiles: string[];
    /**
     * Environment property sources list
     */
    propertySources: IEnvironmentPropertySource[];
}

export interface IEnvironmentTableRow extends ITableRow {
    /**
     * True if propertyValue is primary, false otherwise
     */
    isPrimary: boolean;

    /**
     * flag that designates that the bean is the config props bean.
     */
    configPropsBeanName: string | null;
}
