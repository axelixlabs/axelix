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

import com.axelixlabs.axelix.common.api.BeansFeed;

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
                      "name": "com.axelixlabs.axelix.sbs.autoconfiguration.AxelixBeansAutoConfiguration",
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

        assertThat(beansFeed.getContexts()).hasEntrySatisfying("application", context -> {
            assertThat(context.getParentId()).isEqualTo("parentContext");
            assertThat(context.getBeans()).hasSize(3);

            BeansFeed.Bean first = context.getBeans()
                    .get(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.getAliases()).containsOnly("abc", "bcd");
            assertThat(first.getAutoConfigurationRef())
                    .isEqualTo("DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration");
            assertThat(first.getDependencies()).isEmpty();
            assertThat(first.getScope()).isEqualTo("singleton");
            assertThat(first.getProxyType()).isEqualTo(BeansFeed.ProxyType.JDK_PROXY);
            assertThat(first.getType())
                    .isEqualTo(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.isLazyInit()).isFalse();
            assertThat(first.isPrimary()).isTrue();
            assertThat(first.isConfigPropsBean()).isTrue();
            assertThat(first.getQualifiers()).containsOnly("qualifier1");
            assertThat(first.getBeanSource()).isInstanceOf(BeansFeed.ComponentVariant.class);

            BeansFeed.Bean second = context.getBeans()
                    .get("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.getAliases()).isEmpty();
            assertThat(second.getAutoConfigurationRef())
                    .isEqualTo("HibernateJpaConfiguration#entityManagerFactoryBuilder");
            assertThat(second.getDependencies())
                    .hasSize(2)
                    .satisfiesExactlyInAnyOrder(
                            dep -> {
                                assertThat(dep.getName())
                                        .isEqualTo(
                                                "spring.jpa-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties");
                                assertThat(dep.isConfigPropsDependency()).isTrue();
                            },
                            dep -> {
                                assertThat(dep.getName())
                                        .isEqualTo(
                                                "com.axelixlabs.axelix.sbs.autoconfiguration.AxelixBeansAutoConfiguration");
                                assertThat(dep.isConfigPropsDependency()).isFalse();
                            });
            assertThat(second.getScope()).isEqualTo("singleton");
            assertThat(second.getProxyType()).isEqualTo(BeansFeed.ProxyType.CGLIB);
            assertThat(second.getType())
                    .isEqualTo("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.isLazyInit()).isTrue();
            assertThat(second.isPrimary()).isFalse();
            assertThat(second.isConfigPropsBean()).isFalse();
            assertThat(second.getQualifiers()).isEmpty();
            assertThat(second.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) second.getBeanSource())
                    .extracting(BeansFeed.BeanMethod::getMethodName)
                    .isEqualTo("entityManagerFactoryBuilder");

            assertThat((BeansFeed.BeanMethod) second.getBeanSource())
                    .extracting(BeansFeed.BeanMethod::getEnclosingClassName)
                    .isEqualTo("HibernateJpaConfiguration");

            assertThat((BeansFeed.BeanMethod) second.getBeanSource())
                    .extracting(BeansFeed.BeanMethod::getEnclosingClassFullName)
                    .isEqualTo("org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration");

            BeansFeed.Bean third = context.getBeans()
                    .get("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.getAliases()).isEmpty();
            assertThat(third.getAutoConfigurationRef()).isNull();
            assertThat(third.getDependencies()).isEmpty();
            assertThat(third.getScope()).isEqualTo("singleton");
            assertThat(third.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(third.getType())
                    .isEqualTo("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.isLazyInit()).isFalse();
            assertThat(third.isPrimary()).isFalse();
            assertThat(third.isConfigPropsBean()).isTrue();
            assertThat(third.getQualifiers()).containsOnly("main", "secondary");
            assertThat(third.getBeanSource()).isInstanceOf(BeansFeed.FactoryBean.class);
            assertThat((BeansFeed.FactoryBean) third.getBeanSource())
                    .extracting(BeansFeed.FactoryBean::getFactoryBeanName)
                    .isEqualTo("org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean");
        });
    }
}
