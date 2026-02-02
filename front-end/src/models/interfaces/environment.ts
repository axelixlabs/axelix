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
import type { EPropertyInjectionType } from "models";

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
