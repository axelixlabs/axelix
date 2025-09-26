import type { ICommonSliceState } from "./globals";

export interface IBean {
  /**
   * Name of single bean
   */
  beanName: string;
  /**
   * Name of single bean scope
   */
  scope: string;
  /**
   * Name of single bean classname
   */
  className: string;
  /**
   * Bean aliases list
   */
  aliases: string[];
  /**
   * Bean dependecies
   */
  dependencies: string[];
}

export interface IBeansSliceState extends ICommonSliceState {
  /**
   * Full list of beans
   */
  beans: IBean[];
  /**
   * Search text used for filtering beans
   */
  beansSearchText: string;
  /**
   * Filtered beans after searching
   */
  filteredBeans: IBean[];
}

export interface IBeansCollapseHeaderRefs {
  /**
   * Refs to bean collapse headers, used for smooth scrolling.
   */
  [key: string]: HTMLDivElement | null;
}
