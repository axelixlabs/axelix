import type { ICommonSliceState, IKeyValuePair } from "./globals";

export interface IConfigPropsBean {
  /**
   * The name of the configuration bean
   */
  beanName: string;
  /**
   * The prefix of the configuration bean
   */
  prefix: string;
  /**
   * List of properties of the configuration bean
   */
  properties: IKeyValuePair[];
}

export interface IConfigPropsSliceState extends ICommonSliceState {
  /**
   * List of configuration beans
   */
  beans: IConfigPropsBean[];
  /**
   * Search text used for filtering configuration beans
   */
  configPropsSearchText: string;
  /**
   * Filtered configuration beans after searching
   */
  filteredConfigProps: IConfigPropsBean[];
}
