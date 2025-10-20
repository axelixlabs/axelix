import type { ICommonSliceState, IKeyValuePair } from "./globals";

export interface IEnvironmentPropertySource {
  /**
   * Environment property source name
   */
  name: string;
  /**
   * Environment properties list
   */
  properties: IKeyValuePair[];
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

export interface IEnvironmentSliceState extends ICommonSliceState, IEnvironmentData { }
