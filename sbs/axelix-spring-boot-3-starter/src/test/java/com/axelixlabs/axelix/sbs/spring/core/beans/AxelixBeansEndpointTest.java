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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

/**
 * Integration tests for {@link AxelixBeansEndpoint}.
 *
 * @author Mikhail Polivakha
 */
class AxelixBeansEndpointTest extends AbstractBeansSharedContextTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @Test
    void shouldReturnEnrichedBeansFeed() {

        // when.
        ResponseEntity<BeansFeed> response =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-beans", BeansFeed.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        BeansFeed beanNameToBeanProfile = response.getBody();

        assertQualifiersPostProcessorBean(beanNameToBeanProfile);
        assertBeanMetaInfoExtractor(beanNameToBeanProfile);
        assertCustomBeanSupplier(beanNameToBeanProfile);
        assertConfigPropsBeanName(beanNameToBeanProfile);
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-beans")
    void negativeAuthTests() {}

    private static void assertQualifiersPostProcessorBean(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, CommonConfiguration.QUALIFIERS_PERSISTENCE_POST_PROCESSOR);

        assertThat(bean.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.getBeanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.getMethodName())
                            .isEqualTo(CommonConfiguration.QUALIFIERS_PERSISTENCE_POST_PROCESSOR);
                    assertThat(beanMethod.getEnclosingClassName()).isEqualTo(CommonConfiguration.class.getSimpleName());
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(QualifiersPersistencePostProcessor.class.getName());
    }

    private static void assertBeanMetaInfoExtractor(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, CommonConfiguration.BEAN_META_INFO_EXTRACTOR);

        assertThat(bean.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.getBeanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.getMethodName()).isEqualTo(CommonConfiguration.BEAN_META_INFO_EXTRACTOR);
                    assertThat(beanMethod.getEnclosingClassName()).isEqualTo(CommonConfiguration.class.getSimpleName());
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies())
                .hasSize(2)
                .contains(new BeansFeed.BeanDependency(
                        "conditionalBeanRefBuilder", false)); // second bean is the application context itself
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(DefaultBeanMetaInfoExtractor.class.getName());
    }

    private static void assertCustomBeanSupplier(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, CommonConfiguration.CUSTOM_SUPPLIER);

        assertThat(bean.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.getBeanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.getMethodName()).isEqualTo(CommonConfiguration.CUSTOM_SUPPLIER);
                    assertThat(beanMethod.getEnclosingClassName()).isEqualTo(CommonConfiguration.class.getSimpleName());
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(Supplier.class.getName());
    }

    private static void assertConfigPropsBeanName(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, AxelixPropTest.class.getName());

        assertThat(bean.getClassName()).isEqualTo(AxelixPropTest.class.getName());
        assertThat(bean.getBeanSource()).isNotNull();
        assertThat(bean.isConfigPropsBean()).isTrue();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
    }

    private static BeansFeed.Bean getBean(BeansFeed beanNameToBeanFeed, String beanName) {
        return beanNameToBeanFeed.getBeans().stream()
                .filter(bean -> bean.getBeanName().equals(beanName))
                .findFirst()
                .orElseThrow();
    }
}
