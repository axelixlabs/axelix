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
package com.nucleonforge.axelix.common.api;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The flattened version of conditions response from the actuator endpoint.
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
