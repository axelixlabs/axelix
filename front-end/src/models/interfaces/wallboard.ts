import type { ICommonSliceState } from "./globals";

export interface IInstanceCard  {
    springBootVersion: string;
    javaVersion: string;
    status: string;
    serviceName: string; 
    serviceVersion: string;
    commitHash: string;
    deployedAt: string
}

export interface IServiceCardsData {
    applications: IInstanceCard[];
}

export interface IWallboardSliceState extends ICommonSliceState {
  instances: IInstanceCard[];
  filteredInstances: IInstanceCard[];
  serviceCardsSearchText: string
}
