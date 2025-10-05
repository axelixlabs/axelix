import type { ICommonSliceState } from "./globals";

export interface IServiceCard {
    springBootVersion: string;
    javaVersion: string;
    status: string;
    serviceName: string; 
    serviceVersion: string;
    commitHash: string;
}

export interface IServiceCardsData {
    applications: IServiceCard[];
}

export interface IWallboardSliceState extends ICommonSliceState {
  serviceCards: IServiceCard[];
  filteredServiceCards: IServiceCard[];
  serviceCardsSearchText: string
}
