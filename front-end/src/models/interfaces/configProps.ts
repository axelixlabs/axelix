import type { ICommonSliceState, IKeyValuePair } from "./globals";

export interface IConfigPropsBean {
    /**
     * The name of the configuration properties bean
     */
    beanName: string;
    /**
     * The common prefix of the properties inside the given configuration properties bean
     */
    prefix: string;
    /**
     * List of properties of the configuration properties bean. The keys are prefix-less, meaning,
     * that the common prefix is omitted
     */
    properties: IKeyValuePair[];
}

export interface IConfigPropsBeanData {
    /**
     * Full list of configuration properties beans
     */
    beans: IConfigPropsBean[];
}

export interface IConfigPropsSliceState extends ICommonSliceState, IConfigPropsBeanData {}
