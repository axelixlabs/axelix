/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConditionsFeed;

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

        assertThat(conditionsFeed.positiveConditions()).hasSize(2);

        var positive1 = conditionsFeed.positiveConditions().get(0);
        assertThat(positive1.target()).isEqualTo("EndpointAutoConfiguration#propertiesEndpointAccessResolver");
        assertThat(positive1.matches()).hasSize(1);
        assertThat(positive1.matches().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive1.matches().get(0).message()).contains("@ConditionalOnMissingBean");

        var positive2 = conditionsFeed.positiveConditions().get(1);
        assertThat(positive2.target()).isEqualTo("EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.matches()).hasSize(1);
        assertThat(positive2.matches().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive2.matches().get(0).message()).contains("CachingOperationInvokerAdvisor");

        assertThat(conditionsFeed.negativeConditions()).hasSize(2);

        var negative1 = conditionsFeed.negativeConditions().get(0);
        assertThat(negative1.target()).isEqualTo("WebFluxEndpointManagementContextConfiguration");
        assertThat(negative1.notMatched()).hasSize(1);
        assertThat(negative1.notMatched().get(0).condition()).isEqualTo("OnWebApplicationCondition");
        assertThat(negative1.notMatched().get(0).message()).isEqualTo("not a reactive web application");
        assertThat(negative1.matched()).hasSize(1);
        assertThat(negative1.matched().get(0).condition()).isEqualTo("OnClassCondition");
        assertThat(negative1.matched().get(0).message()).contains("DispatcherHandler");

        var negative2 = conditionsFeed.negativeConditions().get(1);
        assertThat(negative2.target())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration");
        assertThat(negative2.notMatched()).hasSize(1);
        assertThat(negative2.notMatched().get(0).condition())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition");
        assertThat(negative2.notMatched().get(0).message()).contains("AnyNestedCondition");
        assertThat(negative2.matched()).isEmpty();
    }
}
