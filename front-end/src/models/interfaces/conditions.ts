/**
 * Condition data
 */
export interface ICondition {
    condition: string;
    message: string;
}

/**
 * Positive condition matches
 */
export interface IConditionBeanPositive {
    /**
     * The short name of the autoconfiguration class.
     */
    className: string;

    /**
     * The name of the method inside the autoconfiguration class that produced the bean.
     * It might not present in case the condition is defined for the class as a whole, not
     * for the method.
     *
     * Example can be found here:
     * {@link https://github.com/spring-cloud/spring-cloud-openfeign/blob/main/spring-cloud-openfeign-core/src/main/java/org/springframework/cloud/openfeign/FeignAutoConfiguration.java#L89 Spring Cloud's FeignAutoConfiguration}
     */
    methodName?: string;

    /**
     * The list of Spring Boot's autoconfiguration conditions that did match
     */
    matched: ICondition[];
}

/**
 * Negative condition matches
 */
export interface IConditionBeanNegative extends IConditionBeanPositive {
    /**
     * The list of Spring Boot's autoconfiguration conditions that did not match
     */
    notMatched: ICondition[];
}

/**
 * All condition data received from the server
 */
export interface IConditionsResponseBody {
    negativeMatches: IConditionBeanNegative[];
    positiveMatches: IConditionBeanPositive[];
}
