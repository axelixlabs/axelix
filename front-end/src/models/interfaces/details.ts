import type { JSX } from "react";

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
