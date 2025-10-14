import type { ICommonSliceState } from "./globals";

export enum ProxyType {
    JDK_PROXY = "JDK_PROXY",
    CGLIB = "CGLIB",
    NO_PROXYING = "NO_PROXYING",
}

/**
 * An interface that represents the state of the particular bean inside the Spring Boot application
 */
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
   * Name of single bean class name
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

  /**
   * The source from which the bean came from.
   */
  beanSource: IBeanSource;
}

/**
 * Represents the actual algorithm of how the Spring Framework found this bean
 */
export enum BeanOrigin {
  BEAN_METHOD = "BEAN_METHOD",
  COMPONENT_ANNOTATION = "COMPONENT_ANNOTATION",
  FACTORY_BEAN = "FACTORY_BEAN",
  UNKNOWN = "UNKNOWN",
}

/**
 * The "source" of the bean. By "source" we mean how exactly this particular {@link IBean} was discovered
 * by the Spring Framework
 */
export interface IBeanSource {

  /**
   * Optional name of the method that actually produces the instance of the given bean. Present in response from the
   * backend only in case of {@link origin} is equal to {@link BeanOrigin.BEAN_METHOD}
   */
  methodName?: string;

  /**
   * Optional fully qualified name of the enclosing @Configuration class from which the bean came from. Not
   * to be confused with {@link IBean.className}. Present in response from the backend only in case of {@link origin}
   * is equal to {@link BeanOrigin.BEAN_METHOD}
   */
  enclosingClassName?: string;

  /**
   * Optional name of the factory bean name. Present in response from the server only if
   * {@link origin} is equal to {@link BeanOrigin.FACTORY_BEAN}
   */
  factoryBeanName?: string;

  /**
   * The actual origin
   */
  origin: BeanOrigin;
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
