import type { ICommonSliceState } from "./globals";

export interface IUpdatePropertyData {
    /**
     * Property name
     */
    propertyName: string;
    /**
     * New property value
     */
    newValue: string;
}

export interface IUpdateProperty {
    /**
     * Instance id of service
     */
    instanceId: string;
    /**
     * Update property data
     */
    updatePropertyData: IUpdatePropertyData;
}

export interface IUpdatePropertySliceState extends ICommonSliceState {
    /**
     * Success flag for property update
     */
    changePropertySuccess: boolean;
    /**
     * Loading flag for property update
     */
    changePropertyloading: boolean;
}