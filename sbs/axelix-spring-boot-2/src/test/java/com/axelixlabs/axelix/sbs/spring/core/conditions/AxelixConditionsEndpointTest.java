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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.ConditionsFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConditionsEndpoint}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"axelix.prop.test.name=axelix-beans", "axelix.conditions.test.flag=enabled"})
class AxelixConditionsEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @TestConfiguration
    static class AxelixConditionsEndpointTestConfiguration {
        @Bean
        public ConditionalTargetUnwrapper conditionalNameUnwrap() {
            return new DefaultConditionalTargetUnwrapper();
        }

        @Bean
        public ConditionalFeedBuilder conditionalFeedBuilder(
                ConfigurableApplicationContext configurableApplicationContext,
                ConditionalTargetUnwrapper conditionalTargetUnwrapper) {
            return new DefaultConditionalFeedBuilder(configurableApplicationContext, conditionalTargetUnwrapper);
        }

        @Bean
        public AxelixConditionsEndpoint axelixConditionsEndpoint(ConditionalFeedBuilder conditionalFeedBuilder) {
            return new AxelixConditionsEndpoint(conditionalFeedBuilder);
        }

        @Bean
        @ConditionalOnProperty(name = "axelix.conditions.test.flag", havingValue = "enabled")
        public String positiveConditionBean() {
            return "positive";
        }

        @Bean
        @ConditionalOnProperty(name = "axelix.conditions.test.flag", havingValue = "disabled")
        public String negativeConditionBean() {
            return "negative";
        }
    }

    @Test
    void shouldReturnConditionsFeed() {
        // given.
        String className = AxelixConditionsEndpointTest.class.getSimpleName()
                + "."
                + AxelixConditionsEndpointTestConfiguration.class.getSimpleName();
        String positiveMethodName = "positiveConditionBean";
        String negativeMethodName = "negativeConditionBean";

        // when.
        ResponseEntity<ConditionsFeed> response =
                testRestTemplate.getForEntity("/actuator/axelix-conditions", ConditionsFeed.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPositiveMatches()).isNotNull().isNotEmpty();
        assertThat(response.getBody().getPositiveMatches())
                .filteredOn(positiveCondition -> positiveMethodName.equals(positiveCondition.getMethodName()))
                .singleElement()
                .satisfies(positiveCondition -> {
                    assertThat(positiveCondition.getClassName()).isEqualTo(className);
                    assertThat(positiveCondition.getMethodName()).isEqualTo(positiveMethodName);
                    assertThat(positiveCondition.getMatched()).isNotNull().isNotEmpty();
                    assertThat(positiveCondition.getMatched()).allSatisfy(match -> {
                        assertThat(match.getCondition()).isNotBlank();
                        assertThat(match.getMessage()).isNotBlank();
                    });
                });
        assertThat(response.getBody().getNegativeMatches())
                .filteredOn(negativeCondition -> negativeMethodName.equals(negativeCondition.getMethodName()))
                .singleElement()
                .satisfies(negativeCondition -> {
                    assertThat(negativeCondition.getClassName()).isEqualTo(className);
                    assertThat(negativeCondition.getMethodName()).isEqualTo(negativeMethodName);
                    assertThat(negativeCondition.getNotMatched()).isNotNull().isNotEmpty();
                    assertThat(negativeCondition.getMatched()).isNotNull();
                    assertThat(negativeCondition.getNotMatched()).allSatisfy(match -> {
                        assertThat(match.getCondition()).isNotBlank();
                        assertThat(match.getMessage()).isNotBlank();
                    });
                });
    }
}
