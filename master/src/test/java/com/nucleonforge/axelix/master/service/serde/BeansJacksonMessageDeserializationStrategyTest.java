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
package com.nucleonforge.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.api.BeansFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BeansJacksonMessageDeserializationStrategy}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class BeansJacksonMessageDeserializationStrategyTest {

    private final BeansJacksonMessageDeserializationStrategy subject =
            new BeansJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeBeansFeed() {
        // language=json
        String response =
                """
        {
          "contexts" : {
            "application" : {
              "parentId" : "parentContext",
              "beans" : {
                "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration" : {
                  "aliases" : [ "abc", "bcd" ],
                  "scope" : "singleton",
                  "proxyType" : "JDK_PROXY",
                  "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration",
                  "autoConfigurationRef" : "DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration",
                  "dependencies" : [ ],
                  "isLazyInit" : false,
                  "isPrimary" : true,
                  "isConfigPropsBean": true,
                  "qualifiers" : [ "qualifier1" ],
                  "beanSource": {
                     "origin": "COMPONENT_ANNOTATION"
                  }
                },
                "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration" : {
                  "aliases" : [ ],
                  "scope" : "singleton",
                  "proxyType" : "CGLIB",
                  "type" : "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
                  "autoConfigurationRef" : "HibernateJpaConfiguration#entityManagerFactoryBuilder",
                  "dependencies": [
                    {
                      "name": "spring.jpa-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties",
                      "isConfigPropsDependency": true
                    },
                    {
                      "name": "com.nucleonforge.axelix.sbs.autoconfiguration.spring.BeanAnalyzerAutoConfiguration",
                      "isConfigPropsDependency": false
                    }
                 ],
                  "isLazyInit" : true,
                  "isPrimary" : false,
                  "isConfigPropsBean": false,
                  "qualifiers" : [ ],
                  "beanSource": {
                    "enclosingClassName": "HibernateJpaConfiguration",
                    "enclosingClassFullName": "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration",
                    "methodName": "entityManagerFactoryBuilder",
                    "origin": "BEAN_METHOD"
                  }
                },
                "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration" : {
                  "aliases" : [ ],
                  "scope" : "singleton",
                  "proxyType" : "NO_PROXYING",
                  "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
                  "autoConfigurationRef" : null,
                  "dependencies" : [ ],
                  "isLazyInit" : false,
                  "isPrimary" : false,
                  "isConfigPropsBean": true,
                  "qualifiers" : [ "main", "secondary" ],
                  "beanSource": {
                    "factoryBeanName": "org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean",
                    "origin": "FACTORY_BEAN"
                  }
                }
              }
            }
          }
        }
        """;

        BeansFeed beansFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(beansFeed.contexts()).hasEntrySatisfying("application", context -> {
            assertThat(context.parentId()).isEqualTo("parentContext");
            assertThat(context.beans()).hasSize(3);

            BeansFeed.Bean first = context.beans()
                    .get(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.aliases()).containsOnly("abc", "bcd");
            assertThat(first.autoConfigurationRef())
                    .isEqualTo("DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration");
            assertThat(first.dependencies()).isEmpty();
            assertThat(first.scope()).isEqualTo("singleton");
            assertThat(first.proxyType()).isEqualTo(BeansFeed.ProxyType.JDK_PROXY);
            assertThat(first.type())
                    .isEqualTo(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.isLazyInit()).isFalse();
            assertThat(first.isPrimary()).isTrue();
            assertThat(first.isConfigPropsBean()).isTrue();
            assertThat(first.qualifiers()).containsOnly("qualifier1");
            assertThat(first.beanSource()).isInstanceOf(BeansFeed.ComponentVariant.class);

            BeansFeed.Bean second = context.beans()
                    .get("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.aliases()).isEmpty();
            assertThat(second.autoConfigurationRef())
                    .isEqualTo("HibernateJpaConfiguration#entityManagerFactoryBuilder");
            assertThat(second.dependencies())
                    .hasSize(2)
                    .satisfiesExactlyInAnyOrder(
                            dep -> {
                                assertThat(dep.name())
                                        .isEqualTo(
                                                "spring.jpa-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties");
                                assertThat(dep.isConfigPropsDependency()).isTrue();
                            },
                            dep -> {
                                assertThat(dep.name())
                                        .isEqualTo(
                                                "com.nucleonforge.axelix.sbs.autoconfiguration.spring.BeanAnalyzerAutoConfiguration");
                                assertThat(dep.isConfigPropsDependency()).isFalse();
                            });
            assertThat(second.scope()).isEqualTo("singleton");
            assertThat(second.proxyType()).isEqualTo(BeansFeed.ProxyType.CGLIB);
            assertThat(second.type())
                    .isEqualTo("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.isLazyInit()).isTrue();
            assertThat(second.isPrimary()).isFalse();
            assertThat(second.isConfigPropsBean()).isFalse();
            assertThat(second.qualifiers()).isEmpty();
            assertThat(second.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) second.beanSource())
                    .extracting(BeansFeed.BeanMethod::methodName)
                    .isEqualTo("entityManagerFactoryBuilder");

            assertThat((BeansFeed.BeanMethod) second.beanSource())
                    .extracting(BeansFeed.BeanMethod::enclosingClassName)
                    .isEqualTo("HibernateJpaConfiguration");

            assertThat((BeansFeed.BeanMethod) second.beanSource())
                    .extracting(BeansFeed.BeanMethod::enclosingClassFullName)
                    .isEqualTo("org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration");

            BeansFeed.Bean third = context.beans()
                    .get("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.aliases()).isEmpty();
            assertThat(third.autoConfigurationRef()).isNull();
            assertThat(third.dependencies()).isEmpty();
            assertThat(third.scope()).isEqualTo("singleton");
            assertThat(third.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(third.type())
                    .isEqualTo("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.isLazyInit()).isFalse();
            assertThat(third.isPrimary()).isFalse();
            assertThat(third.isConfigPropsBean()).isTrue();
            assertThat(third.qualifiers()).containsOnly("main", "secondary");
            assertThat(third.beanSource()).isInstanceOf(BeansFeed.FactoryBean.class);
            assertThat((BeansFeed.FactoryBean) third.beanSource())
                    .extracting(BeansFeed.FactoryBean::factoryBeanName)
                    .isEqualTo("org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean");
        });
    }
}
