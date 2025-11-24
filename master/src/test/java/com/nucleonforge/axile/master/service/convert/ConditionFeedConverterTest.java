package com.nucleonforge.axile.master.service.convert;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.master.api.response.ConditionsFeedResponse;
import com.nucleonforge.axile.master.service.convert.response.BeansFeedConverter;
import com.nucleonforge.axile.master.service.convert.response.ConditionFeedConverter;

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
                conditionsFeedResponse, "EndpointAutoConfiguration#propertiesEndpointAccessResolver");
        assertThat(positive1.target()).isEqualTo("EndpointAutoConfiguration#propertiesEndpointAccessResolver");
        assertThat(positive1.matched()).hasSize(1);
        assertThat(positive1.matched().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive1.matched().get(0).message()).contains("@ConditionalOnMissingBean");

        var positive2 = getPositiveByTarget(
                conditionsFeedResponse, "EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.target()).isEqualTo("EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.matched()).hasSize(1);
        assertThat(positive2.matched().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive2.matched().get(0).message()).contains("CachingOperationInvokerAdvisor");

        assertThat(conditionsFeedResponse.negativeMatches()).hasSize(2);

        var negative1 = getNegativeByTarget(conditionsFeedResponse, "WebFluxEndpointManagementContextConfiguration");
        assertThat(negative1.target()).isEqualTo("WebFluxEndpointManagementContextConfiguration");
        assertThat(negative1.notMatched()).hasSize(1);
        assertThat(negative1.notMatched().get(0).condition()).isEqualTo("OnWebApplicationCondition");
        assertThat(negative1.notMatched().get(0).message()).isEqualTo("not a reactive web application");
        assertThat(negative1.matched()).hasSize(1);
        assertThat(negative1.matched().get(0).condition()).isEqualTo("OnClassCondition");
        assertThat(negative1.matched().get(0).message()).contains("DispatcherHandler");

        var negative2 = getNegativeByTarget(
                conditionsFeedResponse, "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration");
        assertThat(negative2.target())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration");
        assertThat(negative2.notMatched()).hasSize(1);
        assertThat(negative2.notMatched().get(0).condition())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition");
        assertThat(negative2.notMatched().get(0).message()).contains("AnyNestedCondition");
        assertThat(negative2.matched()).isEmpty();
    }

    private static ConditionsFeedResponse.PositiveCondition getPositiveByTarget(
            ConditionsFeedResponse response, String target) {
        return response.positiveMatches().stream()
                .filter(c -> c.target().equals(target))
                .findFirst()
                .orElseThrow();
    }

    private static ConditionsFeedResponse.NegativeCondition getNegativeByTarget(
            ConditionsFeedResponse response, String target) {
        return response.negativeMatches().stream()
                .filter(c -> c.target().equals(target))
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
