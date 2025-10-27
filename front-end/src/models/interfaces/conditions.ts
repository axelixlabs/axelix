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
    target: string;
    matched: ICondition[];
}

/**
 * Negative condition matches
 */
export interface IConditionBeanNegative extends IConditionBeanPositive {
    notMatched: ICondition[];
}

/**
 * All condition data received from the server
 */
export interface IConditionsResponseBody {
    negativeMatches: IConditionBeanNegative[];
    positiveMatches: IConditionBeanPositive[];
}
