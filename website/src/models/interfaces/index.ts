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
import { ReactNode } from "react";

import { EInstallConfigurationVariant, EInstallMethod, ESpringBootVariant } from "../enums";

export interface INavLink {
    href: string;
    label: string;
}

export interface IInstallSpringBootOptions {
    key: ESpringBootVariant;
    label: string;
}

export interface IInstallConfigurationOptions {
    key: EInstallConfigurationVariant;
    label: string;
}

export interface IInstallMethods {
    key: EInstallMethod;
    label: string;
    icon: ReactNode;
}

export interface IInstallMethodData {
    description: string;
    href: string;
}

export interface IGithubReleaseResponseBody {
    tag_name: string;
}

export interface IAxelixVersionData {
    version: string | null;
    loading: boolean;
}
