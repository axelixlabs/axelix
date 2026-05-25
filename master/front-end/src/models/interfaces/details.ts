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
import type { JSX } from "react";

import type { EExportableComponent } from "../enums/details";

export interface IDetailsGit {
    commitShaShort: string;
    branch: string;
    authorName: string;
    authorEmail: string;
    commitTimestamp: string;
}

export interface IDetailsRuntime {
    javaVersion: string;
    jdkVendor: string;
    garbageCollector: string;
    kotlinVersion?: string;
}

export interface IDetailsSpring {
    springBootVersion: string;
    springFrameworkVersion: string;
    springCloudVersion?: string;
}

export interface IDetailsBuild {
    artifact: string;
    version: string;
    group: string;
    time: string;
}

export interface IDetailsOS {
    name: string;
    version: string;
    arch: string;
}

export interface IDetailsResponseBody {
    serviceName: string;
    git: IDetailsGit;
    runtime: IDetailsRuntime;
    spring: IDetailsSpring;
    build: IDetailsBuild;
    os: IDetailsOS;
}

/**
 * Single Details Card Record
 */
export interface IDetailsCardRecord {
    key: string;
    value: string | JSX.Element;
}

/**
 * The component of state to be exported
 */
interface IStateExportComponent {
    component: EExportableComponent;
}

/**
 * Body of an http request for state export
 */
export interface IStateExportRequestBody {
    components: IStateExportComponent[];
}

/**
 * Request for state export
 */
export interface IStateExportRequest {
    instanceId: string;
    body: IStateExportRequestBody;
}
