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