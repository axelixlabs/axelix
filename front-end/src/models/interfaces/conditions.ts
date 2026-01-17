/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
