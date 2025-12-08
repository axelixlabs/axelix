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
