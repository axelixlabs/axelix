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
import type { EPropertyInjectionType } from "models/enums/environments";

interface IDeprecation {
    /**
     * The message for deprecation
     */
    message: string;
}

export interface IInjectionPoint {
    /**
     * The bean name of injection point
     */
    beanName: string;

    /**
     * The type of the injection point.
     */
    injectionType: EPropertyInjectionType;

    /**
     * The name of the "target". Can be the name of the method in
     * case of method injection, the name of the field in case of
     * a field injection, or a parameter name/number.
     */
    targetName: string;

    /**
     * The expression of the property
     */
    propertyExpression: string;
}

export interface IEnvProperty {
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
     * Flag that designates that the bean is the config props bean.
     */
    configPropsBeanName: string | null;

    /**
     * The property description
     */
    description: string | null;

    /**
     * If this property exists, then the underlying spring environment's property is deprecated
     */
    deprecation?: IDeprecation;

    /**
     * The injection points list
     */
    injectionPoints?: IInjectionPoint[];
}

export interface IEnvironmentPropertySource {
    /**
     * Environment property source name
     */
    name: string;

    /**
     * The description of property source
     */
    description: string | null;

    /**
     * Environment properties list
     */
    properties: IEnvProperty[];
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
