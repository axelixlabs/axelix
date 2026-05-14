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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.shared.AxelixPropTest;
import com.axelixlabs.axelix.sbs.spring.core.shared.SharedEndpointTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

/**
 * Integration tests for {@link AxelixBeansEndpoint}.
 *
 * @author Mikhail Polivakha
 */
class AxelixBeansEndpointTest extends AbstractEndpointTest {

    private static final String QUALIFIERS_PERSISTENCE_POST_PROCESSOR = "qualifiersPersistencePostProcessor";
    private static final String BEAN_META_INFO_EXTRACTOR = "beanMetaInfoExtractor";
    private static final String CUSTOM_SUPPLIER = SharedEndpointTestConfiguration.CUSTOM_SUPPLIER;

    private static final String ENCLOSING_CLASS_SIMPLE_NAME = SharedEndpointTestConfiguration.class.getSimpleName();

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldReturnEnrichedBeansFeed() {

        // when.
        ResponseEntity<BeansFeed> response = testRestTemplate.getForEntity("/actuator/axelix-beans", BeansFeed.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        BeansFeed beanNameToBeanProfile = response.getBody();

        assertQualifiersPostProcessorBean(beanNameToBeanProfile);
        assertBeanMetaInfoExtractor(beanNameToBeanProfile);
        assertCustomBeanSupplier(beanNameToBeanProfile);
        assertConfigPropsBeanName(beanNameToBeanProfile);
    }

    private static void assertQualifiersPostProcessorBean(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, QUALIFIERS_PERSISTENCE_POST_PROCESSOR);

        assertThat(bean.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.getBeanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.getMethodName()).isEqualTo(QUALIFIERS_PERSISTENCE_POST_PROCESSOR);
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(QualifiersPersistencePostProcessor.class.getName());
    }

    private static void assertBeanMetaInfoExtractor(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, BEAN_META_INFO_EXTRACTOR);

        assertThat(bean.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.getBeanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.getMethodName()).isEqualTo(BEAN_META_INFO_EXTRACTOR);
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).contains(new BeansFeed.BeanDependency("conditionalBeanRefBuilder", false));
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(DefaultBeanMetaInfoExtractor.class.getName());
    }

    private static void assertCustomBeanSupplier(BeansFeed beanNameToBeanFeed) {
        BeansFeed.Bean bean = getBean(beanNameToBeanFeed, CUSTOM_SUPPLIER);

        assertThat(bean.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.getBeanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.getMethodName()).isEqualTo(CUSTOM_SUPPLIER);
                    assertThat(beanMethod.getEnclosingClassName()).isEqualTo(ENCLOSING_CLASS_SIMPLE_NAME);
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
