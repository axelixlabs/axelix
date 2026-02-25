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
import type { IKeyValuePair } from "./globals";

export interface IConfigPropsBean {
    /**
     * The name of the configuration properties bean
     */
    beanName: string;

    /**
     * The common prefix of the properties inside the given configuration properties bean
     */
    prefix: string;

    /**
     * List of properties of the configuration properties bean. The keys are prefix-less, meaning,
     * that the common prefix is omitted
     */
    properties: IKeyValuePair[];
}

export interface IConfigPropsResponseBody {
    /**
     * Full list of configuration properties beans
     */
    beans: IConfigPropsBean[];
}
