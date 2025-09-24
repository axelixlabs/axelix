export interface TableData extends IEnvironmentProperty {
  name: string;
}

export interface IEnvironmentProperty {
  key: string;
  value: string;
}

export interface IEnvironmentPropertySource {
  name: string;
  properties: IEnvironmentProperty[];
}

export interface IEnvironmentData {
  activeProfiles: string[];
  defaultProfiles: string[];
  propertySources: IEnvironmentPropertySource[];
}

export interface IEnvironmentSliceState {
  loading: boolean;
  data: IEnvironmentData;
  error: string;
}
