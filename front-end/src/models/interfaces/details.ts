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
    kotlinVersion: string;
}

export interface IDetailsSpring {
    springBootVersion: string;
    SpringFrameworkVersion: string;
    SpringCloudVersion: string;
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
 * The {@link IStateExportComponent} specifically for heap dumps.
 */
// eslint-disable-next-line  @typescript-eslint/no-unused-vars
interface IHeapStateComponent extends IStateExportComponent {
    sanitized: boolean;
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
