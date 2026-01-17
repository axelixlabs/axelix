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
