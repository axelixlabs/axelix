import type { ICommonSliceState } from "./globals";

export interface IUpdatePropertyData {
    propertyName: string;
    newValue: string;
}

export interface IUpdateProperty {
    instanceId: string;
    updatePropertyData: IUpdatePropertyData;
}

export interface IUpdatePropertySliceState extends ICommonSliceState {
    changePropertySuccess: boolean;
    changePropertyloading: boolean;
}