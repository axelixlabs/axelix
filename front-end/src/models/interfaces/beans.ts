import { EBeanOrigin, EProxyType } from "models";

/**
 * Response for the /beans endpoint
 */
export interface IBeansResponseBody {
    /**
     * The list of beans
     */
    beans: IBean[];
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
    proxyType: EProxyType | null;

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
 * The "source" of the bean. By "source" we mean how exactly this particular {@link IBean} was discovered
 * by the Spring Framework
 */
interface IBeanSource {
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
    origin: EBeanOrigin;
}
