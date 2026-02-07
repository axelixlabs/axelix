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
package com.axelixlabs.axelix.master.service.convert;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.ConditionsFeed;
import com.axelixlabs.axelix.master.api.external.response.ConditionsFeedResponse;
import com.axelixlabs.axelix.master.service.convert.response.BeansFeedConverter;
import com.axelixlabs.axelix.master.service.convert.response.ConditionFeedConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BeansFeedConverter}.
 *
 * @since 16.10.2025
 * @author Niktia Kirillov
 */
class ConditionFeedConverterTest {

    private final ConditionFeedConverter subject = new ConditionFeedConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        ConditionsFeedResponse conditionsFeedResponse =
                subject.convertInternal(new ConditionsFeed(positiveConditions(), negativeConditions()));

        // then.
        assertThat(conditionsFeedResponse.positiveMatches()).hasSize(2);

        var positive1 = getPositiveByTarget(
                conditionsFeedResponse, "EndpointAutoConfiguration", "propertiesEndpointAccessResolver");
        assertThat(positive1.className()).isEqualTo("EndpointAutoConfiguration");
        assertThat(positive1.methodName()).isEqualTo("propertiesEndpointAccessResolver");
        assertThat(positive1.matched()).hasSize(1);
        assertThat(positive1.matched().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive1.matched().get(0).message()).contains("@ConditionalOnMissingBean");

        var positive2 = getPositiveByTarget(
                conditionsFeedResponse, "EndpointAutoConfiguration", "endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.className()).isEqualTo("EndpointAutoConfiguration");
        assertThat(positive2.methodName()).isEqualTo("endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.matched()).hasSize(1);
        assertThat(positive2.matched().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive2.matched().get(0).message()).contains("CachingOperationInvokerAdvisor");

        assertThat(conditionsFeedResponse.negativeMatches()).hasSize(2);

        var negative1 =
                getNegativeByTarget(conditionsFeedResponse, "WebFluxEndpointManagementContextConfiguration", null);
        assertThat(negative1.className()).isEqualTo("WebFluxEndpointManagementContextConfiguration");
        assertThat(negative1.methodName()).isEqualTo(null);
        assertThat(negative1.notMatched()).hasSize(1);
        assertThat(negative1.notMatched().get(0).condition()).isEqualTo("OnWebApplicationCondition");
        assertThat(negative1.notMatched().get(0).message()).isEqualTo("not a reactive web application");
        assertThat(negative1.matched()).hasSize(1);
        assertThat(negative1.matched().get(0).condition()).isEqualTo("OnClassCondition");
        assertThat(negative1.matched().get(0).message()).contains("DispatcherHandler");

        var negative2 = getNegativeByTarget(
                conditionsFeedResponse,
                "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration",
                null);
        assertThat(negative2.className())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration");
        assertThat(negative2.methodName()).isNull();
        assertThat(negative2.notMatched()).hasSize(1);
        assertThat(negative2.notMatched().get(0).condition())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition");
        assertThat(negative2.notMatched().get(0).message()).contains("AnyNestedCondition");
        assertThat(negative2.matched()).isEmpty();
    }

    private static ConditionsFeedResponse.PositiveCondition getPositiveByTarget(
            ConditionsFeedResponse response, String className, String methodName) {
        return response.positiveMatches().stream()
                .filter(c -> Objects.equals(c.methodName(), methodName) && Objects.equals(c.className(), className))
                .findFirst()
                .orElseThrow();
    }

    private static ConditionsFeedResponse.NegativeCondition getNegativeByTarget(
            ConditionsFeedResponse response, String className, String methodName) {
        return response.negativeMatches().stream()
                .filter(c -> Objects.equals(c.methodName(), methodName) && Objects.equals(c.className(), className))
                .findFirst()
                .orElseThrow();
    }

    private static List<ConditionsFeed.PositiveCondition> positiveConditions() {
        return List.of(
                new ConditionsFeed.PositiveCondition(
                        "EndpointAutoConfiguration#propertiesEndpointAccessResolver",
                        List.of(
                                new ConditionsFeed.ConditionMatch(
                                        "OnBeanCondition",
                                        "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.EndpointAccessResolver; SearchStrategy: all) did not find any beans"))),
                new ConditionsFeed.PositiveCondition(
                        "EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor",
                        List.of(
                                new ConditionsFeed.ConditionMatch(
                                        "OnBeanCondition",
                                        "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor; SearchStrategy: all) did not find any beans"))));
    }

    private static List<ConditionsFeed.NegativeCondition> negativeConditions() {
        return List.of(
                new ConditionsFeed.NegativeCondition(
                        "WebFluxEndpointManagementContextConfiguration",
                        List.of(new ConditionsFeed.ConditionMatch(
                                "OnWebApplicationCondition", "not a reactive web application")),
                        List.of(
                                new ConditionsFeed.ConditionMatch(
                                        "OnClassCondition",
                                        "@ConditionalOnClass found required classes 'org.springframework.web.reactive.DispatcherHandler', 'org.springframework.http.server.reactive.HttpHandler'"))),
                new ConditionsFeed.NegativeCondition(
                        "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration",
                        List.of(
                                new ConditionsFeed.ConditionMatch(
                                        "GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition",
                                        "AnyNestedCondition 0 matched 1 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.JacksonJsonbUnavailable NoneNestedConditions")),
                        List.of()));
    }
}
