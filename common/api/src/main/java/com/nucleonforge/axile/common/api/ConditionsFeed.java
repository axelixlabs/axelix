/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.common.api;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to conditions actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/conditions.html">Conditions Endpoint</a>
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
public record ConditionsFeed(List<PositiveCondition> positiveConditions, List<NegativeCondition> negativeConditions) {

    @JsonCreator
    public ConditionsFeed(
            @JsonProperty("positiveConditions") List<PositiveCondition> positiveConditions,
            @JsonProperty("negativeConditions") List<NegativeCondition> negativeConditions) {
        this.positiveConditions = positiveConditions != null ? positiveConditions : Collections.emptyList();
        this.negativeConditions = negativeConditions != null ? negativeConditions : Collections.emptyList();
    }

    public record PositiveCondition(String target, List<ConditionMatch> matches) {
        @JsonCreator
        public PositiveCondition(
                @JsonProperty("target") String target, @JsonProperty("matches") List<ConditionMatch> matches) {
            this.target = target;
            this.matches = matches != null ? matches : Collections.emptyList();
        }
    }

    public record NegativeCondition(String target, List<ConditionMatch> notMatched, List<ConditionMatch> matched) {
        @JsonCreator
        public NegativeCondition(
                @JsonProperty("target") String target,
                @JsonProperty("notMatched") List<ConditionMatch> notMatched,
                @JsonProperty("matched") List<ConditionMatch> matched) {
            this.target = target;
            this.notMatched = notMatched != null ? notMatched : Collections.emptyList();
            this.matched = matched != null ? matched : Collections.emptyList();
        }
    }

    public record ConditionMatch(String condition, String message) {
        @JsonCreator
        public ConditionMatch(@JsonProperty("condition") String condition, @JsonProperty("message") String message) {
            this.condition = condition;
            this.message = message;
        }
    }
}
