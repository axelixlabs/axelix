import type { ICommonSliceState } from "./globals";

interface IProperties {
    /**
     * The property name
     */
    name: string;
    /**
     * The property value
     */
    value: string;
    /**
     * If true, the property is primary
     * */
    isPrimary: boolean;
}

export interface IEnvironmentPropertySource {
    /**
     * Environment property source name
     */
    name: string;
    /**
     * Environment properties list
     */
    properties: IProperties[];
}

export interface IEnvironmentData {
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

export interface IEnvironmentSliceState extends ICommonSliceState, IEnvironmentData {}
