import type { ICommonSliceState, IKeyValuePair } from "./globals";

interface IEnvironmentPropertySource {
  /**
   * Environment property source name
   */
  name: string;
  /**
   * Environment properies list
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

export interface IEnvironmentSliceState extends ICommonSliceState, IEnvironmentData {
  environmentSearchText: string;
  filteredPropertySources: IEnvironmentPropertySource[];
  success: boolean
}

export interface IUpdateEnvConfigData {
  propertyName: string;
  newValue: string;
}

export interface IUpdateEnvConfigProperty {
  instanceId: string;
  data: IUpdateEnvConfigData;
}