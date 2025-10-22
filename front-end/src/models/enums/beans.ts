export enum EProxyType {
    JDK_PROXY = "JDK_PROXY",
    CGLIB = "CGLIB",
    NO_PROXYING = "NO_PROXYING",
}

/**
 * Represents the actual algorithm of how the Spring Framework found this bean
 */
export enum EBeanOrigin {
    BEAN_METHOD = "BEAN_METHOD",
    COMPONENT_ANNOTATION = "COMPONENT_ANNOTATION",
    FACTORY_BEAN = "FACTORY_BEAN",
    UNKNOWN = "UNKNOWN",
}
