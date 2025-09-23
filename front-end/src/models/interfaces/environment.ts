export interface TableData extends IEnvironmantProperty {
  name: string;
}

export interface IEnvironmantProperty {
  key: string;
  value: string;
}

export interface IEnvironmantPropertySource {
  name: string;
  properties: IEnvironmantProperty[];
}

export interface IEnvironmantData {
  activeProfiles: string[];
  defaultProfiles: string[];
  propertySources: IEnvironmantPropertySource[];
}

export interface IEnvironmantSliceState {
  loading: boolean;
  data: IEnvironmantData;
  error: string;
}
