import type { ICommonSliceState } from "./globals";

export enum ProxyType {
    JDK_PROXY = "JDK_PROXY",
    CGLIB = "CGLIB",
    NO_PROXYING = "NO_PROXYING",
}

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
   * The proxying algorithm that is used for this bean. Might be null in case
   * the backend was unable to figure it out.
   */
  proxyType: ProxyType | null;

  /**
   * Qualified of this bean, if any
   */
  qualifiers: string[];

  /**
   * Whether this bean is lazily initialized
   */
  isLazyInit: boolean;

  /**
   * Whether this bean is marked as primary
   */
  isPrimary: boolean;

  /**
   * Bean aliases list
   */
  aliases: string[];
  /**
   * Bean dependencies
   */
  dependencies: string[];
}

export interface IBeansData {
  /**
   * Full list of beans
   */
  beans: IBean[];
}

export interface IBeansSliceState extends ICommonSliceState, IBeansData {
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
