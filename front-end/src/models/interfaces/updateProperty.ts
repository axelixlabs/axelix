import type { ICommonSliceState } from "./globals";

export interface IUpdatePropertyData {
    propertyName: string;
    newValue: string;
    propertySourceName: string;
}


export interface IUpdateProperty {
    instanceId: string;
    data: IUpdatePropertyData;
}

export interface IUpdatePropertySliceState extends ICommonSliceState {
    changePropertySuccess: boolean;
}