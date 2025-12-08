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
export enum EProxyType {
    JDK_PROXY = "JDK_PROXY",
    CGLIB = "CGLIB",
    NO_PROXYING = "NO_PROXYING",
}

/**
 * Represents the actual algorithm of how the Spring Framework found this bean
 */
export enum EBeanOrigin {
    BEAN_METHOD = "BEAN_METHOD",
    COMPONENT_ANNOTATION = "COMPONENT_ANNOTATION",
    FACTORY_BEAN = "FACTORY_BEAN",
    SYNTHETIC_BEAN = "SYNTHETIC_BEAN",
    UNKNOWN = "UNKNOWN",
}

export enum ESearchSubject {
    BEAN_NAME_OR_ALIAS,
    BEAN_CLASS,
}
