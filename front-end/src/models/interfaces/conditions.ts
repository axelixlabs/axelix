/**
 * Condition data
 */
export interface ICondition {
    condition: string;
    message: string;
}

/**
 * Negative condition matches
 */
export interface INegativeMatches {
    target: string;
    notMatched: ICondition[];
    matched: ICondition[];
}

/**
 * Positive condition matches
 */
export interface IPositiveMatches {
    target: string;
    matches: ICondition[];
}

/**
 * All condition data received from the server
 */
export interface IConditionsData {
    negativeMatches: INegativeMatches[];
    positiveMatches: IPositiveMatches[];
}
