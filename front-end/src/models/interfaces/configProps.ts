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

export interface IConfigPropsSliceState extends ICommonSliceState {
  /**
   * List of configuration properties beans
   */
  beans: IConfigPropsBean[];
  /**
   * Search text used for filtering configuration properties beans
   */
  configPropsSearchText: string;
  /**
   * Filtered configuration properties beans after searching
   */
  filteredConfigProps: IConfigPropsBean[];
}
