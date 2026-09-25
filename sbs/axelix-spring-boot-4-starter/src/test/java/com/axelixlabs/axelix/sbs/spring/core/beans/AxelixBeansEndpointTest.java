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

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.BeanDependency;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.BeanMethod;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.BeansFeed;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.ProxyType;
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

    @Test
    void shouldKeepTheWireFormatOfTheBeansFeed() {
        // when.
        ResponseEntity<String> response =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-beans", String.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        String beanPath = "$.beans[?(@.beanName == '" + CommonConfiguration.CUSTOM_SUPPLIER + "')]";

        JsonAssertions.assertThatJson(response.getBody())
                .inPath(beanPath + ".beanSource")
                .isArray()
                .containsExactly(
                        // language=json
                        """
                        {
                          "origin" : "BEAN_METHOD",
                          "enclosingClassName" : "CommonConfiguration",
                          "enclosingClassFullName" : "#{json-unit.any-string}",
                          "methodName" : "customSupplier"
                        }
                        """);
        JsonAssertions.assertThatJson(response.getBody())
                .inPath(beanPath)
                .isArray()
                .first()
                .isObject()
                .containsKeys(
                        "beanName",
                        "className",
                        "scope",
                        "proxyType",
                        "aliases",
                        "dependencies",
                        "isPrimary",
                        "isLazyInit",
                        "isConfigPropsBean",
                        "qualifiers",
                        "beanSource");
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-beans")
    void negativeAuthTests() {}

    private static void assertQualifiersPostProcessorBean(BeansFeed beanNameToBeanFeed) {
        var bean = getBean(beanNameToBeanFeed, CommonConfiguration.QUALIFIERS_PERSISTENCE_POST_PROCESSOR);

        assertThat(bean.getBeanSource()).isInstanceOf(BeanMethod.class);
        assertThat(bean.getBeanSource()).asInstanceOf(type(BeanMethod.class)).satisfies(beanMethod -> {
            assertThat(beanMethod.getMethodName()).isEqualTo(CommonConfiguration.QUALIFIERS_PERSISTENCE_POST_PROCESSOR);
            assertThat(beanMethod.getEnclosingClassName()).isEqualTo(CommonConfiguration.class.getSimpleName());
        });
        assertThat(bean.getIsConfigPropsBean()).isFalse();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.getIsLazyInit()).isFalse();
        assertThat(bean.getIsPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(QualifiersPersistencePostProcessor.class.getName());
    }

    private static void assertBeanMetaInfoExtractor(BeansFeed beanNameToBeanFeed) {
        var bean = getBean(beanNameToBeanFeed, CommonConfiguration.BEAN_META_INFO_EXTRACTOR);

        assertThat(bean.getBeanSource()).isInstanceOf(BeanMethod.class);
        assertThat(bean.getBeanSource()).asInstanceOf(type(BeanMethod.class)).satisfies(beanMethod -> {
            assertThat(beanMethod.getMethodName()).isEqualTo(CommonConfiguration.BEAN_META_INFO_EXTRACTOR);
            assertThat(beanMethod.getEnclosingClassName()).isEqualTo(CommonConfiguration.class.getSimpleName());
        });
        assertThat(bean.getIsConfigPropsBean()).isFalse();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies())
                .hasSize(2)
                .contains(new BeanDependency()
                        .name("conditionalBeanRefBuilder")
                        .isConfigPropsDependency(false)); // second bean is the application context itself
        assertThat(bean.getIsLazyInit()).isFalse();
        assertThat(bean.getIsPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(DefaultBeanMetaInfoExtractor.class.getName());
    }

    private static void assertCustomBeanSupplier(BeansFeed beanNameToBeanFeed) {
        var bean = getBean(beanNameToBeanFeed, CommonConfiguration.CUSTOM_SUPPLIER);

        assertThat(bean.getBeanSource()).isInstanceOf(BeanMethod.class);
        assertThat(bean.getBeanSource()).asInstanceOf(type(BeanMethod.class)).satisfies(beanMethod -> {
            assertThat(beanMethod.getMethodName()).isEqualTo(CommonConfiguration.CUSTOM_SUPPLIER);
            assertThat(beanMethod.getEnclosingClassName()).isEqualTo(CommonConfiguration.class.getSimpleName());
        });
        assertThat(bean.getIsConfigPropsBean()).isFalse();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.getIsLazyInit()).isFalse();
        assertThat(bean.getIsPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
        assertThat(bean.getClassName()).isEqualTo(Supplier.class.getName());
    }

    private static void assertConfigPropsBeanName(BeansFeed beanNameToBeanFeed) {
        var bean = getBean(beanNameToBeanFeed, AxelixPropTest.class.getName());

        assertThat(bean.getClassName()).isEqualTo(AxelixPropTest.class.getName());
        assertThat(bean.getBeanSource()).isNotNull();
        assertThat(bean.getIsConfigPropsBean()).isTrue();
        assertThat(bean.getAutoConfigurationRef()).isNull();
        assertThat(bean.getAliases()).isEmpty();
        assertThat(bean.getDependencies()).isEmpty();
        assertThat(bean.getIsLazyInit()).isFalse();
        assertThat(bean.getIsPrimary()).isFalse();
        assertThat(bean.getQualifiers()).isEmpty();
        assertThat(bean.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
    }

    private static com.axelixlabs.axelix.sbs.spring.core.contract.beans.Bean getBean(
            BeansFeed beanNameToBeanFeed, String beanName) {
        return beanNameToBeanFeed.getBeans().stream()
                .filter(bean -> bean.getBeanName().equals(beanName))
                .findFirst()
                .orElseThrow();
    }
}
