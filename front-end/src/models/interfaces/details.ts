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
import type { JSX } from "react";

import type { EExportableComponent } from "../enums/details.ts";

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
