import type { ICommonSliceState } from "./globals";

export interface IInstanceCard {
  instanceId: string;
  springBootVersion: string;
  javaVersion: string;
  status: string;
  name: string;
  serviceVersion: string;
  commitShaShort: string;
  deployedFor: string
}

export interface IServiceCardsData {
  instances: IInstanceCard[];
}

export interface IWallboardSliceState extends ICommonSliceState {
  instances: IInstanceCard[];
  filteredInstances: IInstanceCard[];
  instancesSearchText: string
}
