import type { ICommonSliceState } from "./globals";

export interface IEnvironmentProperty {
  /**
   * Environment single propery key
   */
  key: string;
  /**
   * Environment single propery value
   */
  value: string;
}

export interface IEnvironmentPropertySource {
  /**
   * Environment propery source name
   */
  name: string;
  /**
   * Environment properies list
   */
  properties: IEnvironmentProperty[];
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

export interface IEnvironmentSliceState extends ICommonSliceState {
  /**
   * Environment data received from backend
   */
  data: IEnvironmentData;
}
