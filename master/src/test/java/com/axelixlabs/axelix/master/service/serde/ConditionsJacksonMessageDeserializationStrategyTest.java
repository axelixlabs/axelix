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
package com.axelixlabs.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.ConditionsFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BeansJacksonMessageDeserializationStrategy}. The json for deserialization was taken from
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/conditions.html">official doc.</a>.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
class ConditionsJacksonMessageDeserializationStrategyTest {

    private final ConditionsJacksonMessageDeserializationStrategy subject =
            new ConditionsJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeConditionsFeed() {
        // language=json
        String response =
                """
        {
          "positiveConditions": [
            {
              "target": "EndpointAutoConfiguration#propertiesEndpointAccessResolver",
              "matches": [
                {
                  "condition": "OnBeanCondition",
                  "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.EndpointAccessResolver; SearchStrategy: all) did not find any beans"
                }
              ]
            },
            {
              "target": "EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor",
              "matches": [
                {
                  "condition": "OnBeanCondition",
                  "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor; SearchStrategy: all) did not find any beans"
                }
              ]
            }
          ],
          "negativeConditions": [
            {
              "target": "WebFluxEndpointManagementContextConfiguration",
              "notMatched": [
                {
                  "condition": "OnWebApplicationCondition",
                  "message": "not a reactive web application"
                }
              ],
              "matched": [
                {
                  "condition": "OnClassCondition",
                  "message": "@ConditionalOnClass found required classes 'org.springframework.web.reactive.DispatcherHandler', 'org.springframework.http.server.reactive.HttpHandler'"
                }
              ]
            },
            {
              "target": "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration",
              "notMatched": [
                {
                  "condition": "GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition",
                  "message": "AnyNestedCondition 0 matched 1 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.JacksonJsonbUnavailable NoneNestedConditions"
                }
              ],
              "matched": []
            }
          ]
        }
        """;

        ConditionsFeed conditionsFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(conditionsFeed.getPositiveConditions()).hasSize(2);

        var positive1 = conditionsFeed.getPositiveConditions().get(0);
        assertThat(positive1.getTarget()).isEqualTo("EndpointAutoConfiguration#propertiesEndpointAccessResolver");
        assertThat(positive1.getMatches()).hasSize(1);
        assertThat(positive1.getMatches().get(0).getCondition()).isEqualTo("OnBeanCondition");
        assertThat(positive1.getMatches().get(0).getMessage()).contains("@ConditionalOnMissingBean");

        var positive2 = conditionsFeed.getPositiveConditions().get(1);
        assertThat(positive2.getTarget()).isEqualTo("EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.getMatches()).hasSize(1);
        assertThat(positive2.getMatches().get(0).getCondition()).isEqualTo("OnBeanCondition");
        assertThat(positive2.getMatches().get(0).getMessage()).contains("CachingOperationInvokerAdvisor");

        assertThat(conditionsFeed.getNegativeConditions()).hasSize(2);

        var negative1 = conditionsFeed.getNegativeConditions().get(0);
        assertThat(negative1.getTarget()).isEqualTo("WebFluxEndpointManagementContextConfiguration");
        assertThat(negative1.getNotMatched()).hasSize(1);
        assertThat(negative1.getNotMatched().get(0).getCondition()).isEqualTo("OnWebApplicationCondition");
        assertThat(negative1.getNotMatched().get(0).getMessage()).isEqualTo("not a reactive web application");
        assertThat(negative1.getMatched()).hasSize(1);
        assertThat(negative1.getMatched().get(0).getCondition()).isEqualTo("OnClassCondition");
        assertThat(negative1.getMatched().get(0).getMessage()).contains("DispatcherHandler");

        var negative2 = conditionsFeed.getNegativeConditions().get(1);
        assertThat(negative2.getTarget())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration");
        assertThat(negative2.getNotMatched()).hasSize(1);
        assertThat(negative2.getNotMatched().get(0).getCondition())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition");
        assertThat(negative2.getNotMatched().get(0).getMessage()).contains("AnyNestedCondition");
        assertThat(negative2.getMatched()).isEmpty();
    }
}
