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
package com.nucleonforge.axile.sbs.spring.beans;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.sbs.spring.conditions.ConditionalBeanRefBuilder;
import com.nucleonforge.axile.sbs.spring.conditions.DefaultConditionalBeanRefBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

/**
 * Integration tests for {@link BeansEndpointExtension}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(
        classes = BeansEndpointExtensionTest.CurrentConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({BeansEndpoint.class, BeansEndpointExtension.class, ConditionsReportEndpoint.class})
class BeansEndpointExtensionTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @TestConfiguration
    static class CurrentConfiguration {

        static final String QUALIFIERS_PERSISTENCE_POST_PROCESSOR = "qualifiersPersistencePostProcessor";
        static final String BEAN_META_INFO_EXTRACTOR = "beanMetaInfoExtractor";
        static final String CUSTOM_SUPPLIER = "customSupplier";

        @Bean
        public ConditionalBeanRefBuilder beanNameNormalizer() {
            return new DefaultConditionalBeanRefBuilder();
        }

        @Bean(BEAN_META_INFO_EXTRACTOR)
        BeanMetaInfoExtractor beanMetaInfoExtractor(
                ConfigurableListableBeanFactory configurableListableBeanFactory,
                ConditionsReportEndpoint delegateConditions,
                ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
            return new DefaultBeanMetaInfoExtractor(
                    configurableListableBeanFactory, delegateConditions, conditionalBeanRefBuilder);
        }

        @Bean(QUALIFIERS_PERSISTENCE_POST_PROCESSOR)
        public static QualifiersPersistencePostProcessor qualifiersPersistencePostProcessor() {
            return new QualifiersPersistencePostProcessor();
        }

        @Bean(CUSTOM_SUPPLIER)
        public Supplier<String> customSupplier() {
            return () -> "value";
        }
    }

    @Test
    void shouldReturnEnrichedBeansFeed() {

        // when.
        ResponseEntity<BeansFeed> response = testRestTemplate.getForEntity("/actuator/beans", BeansFeed.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        var beanNameToBeanProfile = extractBeansFeed(response.getBody());

        assertQualifiersPostProcessorBean(beanNameToBeanProfile);
        assertBeanMetaInfoExtractor(beanNameToBeanProfile);
        assertCustomBeanSupplier(beanNameToBeanProfile);
    }

    private static void assertQualifiersPostProcessorBean(List<Entry<String, BeansFeed.Bean>> beanNameToBeanProfile) {
        BeansFeed.Bean bean =
                getBean(beanNameToBeanProfile, CurrentConfiguration.QUALIFIERS_PERSISTENCE_POST_PROCESSOR);
        assertThat(bean.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.beanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.methodName())
                            .isEqualTo(CurrentConfiguration.QUALIFIERS_PERSISTENCE_POST_PROCESSOR);
                    assertThat(beanMethod.enclosingClassName()).isEqualTo("CurrentConfiguration");
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.autoConfigurationRef()).isNull();
        assertThat(bean.aliases()).isEmpty();
        assertThat(bean.dependencies()).isEmpty();
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.qualifiers()).isEmpty();
        assertThat(bean.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.type()).isEqualTo(QualifiersPersistencePostProcessor.class.getName());
    }

    private static void assertBeanMetaInfoExtractor(List<Entry<String, BeansFeed.Bean>> beanNameToBeanProfile) {
        BeansFeed.Bean bean = getBean(beanNameToBeanProfile, CurrentConfiguration.BEAN_META_INFO_EXTRACTOR);
        assertThat(bean.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.beanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.methodName()).isEqualTo(CurrentConfiguration.BEAN_META_INFO_EXTRACTOR);
                    assertThat(beanMethod.enclosingClassName()).isEqualTo("CurrentConfiguration");
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.autoConfigurationRef()).isNull();
        assertThat(bean.aliases()).isEmpty();
        //        assertThat(bean.dependencies()).isEmpty(); // TODO: Here comes the problem with enclosing class as the
        // dependency
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.qualifiers()).isEmpty();
        assertThat(bean.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.type()).isEqualTo(DefaultBeanMetaInfoExtractor.class.getName());
    }

    private static void assertCustomBeanSupplier(List<Entry<String, BeansFeed.Bean>> beanNameToBeanProfile) {
        BeansFeed.Bean bean = getBean(beanNameToBeanProfile, CurrentConfiguration.CUSTOM_SUPPLIER);
        assertThat(bean.beanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
        assertThat(bean.beanSource())
                .asInstanceOf(type(BeansFeed.BeanMethod.class))
                .satisfies(beanMethod -> {
                    assertThat(beanMethod.methodName()).isEqualTo(CurrentConfiguration.CUSTOM_SUPPLIER);
                    assertThat(beanMethod.enclosingClassName()).isEqualTo("CurrentConfiguration");
                });
        assertThat(bean.isConfigPropsBean()).isFalse();
        assertThat(bean.autoConfigurationRef()).isNull();
        assertThat(bean.aliases()).isEmpty();
        //        assertThat(bean.dependencies()).isEmpty(); // TODO: Here comes the problem with enclosing class as the
        // dependency
        assertThat(bean.isLazyInit()).isFalse();
        assertThat(bean.isPrimary()).isFalse();
        assertThat(bean.qualifiers()).isEmpty();
        assertThat(bean.proxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
        assertThat(bean.type()).isEqualTo(Supplier.class.getName());
    }

    private static BeansFeed.Bean getBean(List<Entry<String, BeansFeed.Bean>> beanNameToBeanProfile, String beanName) {
        return beanNameToBeanProfile.stream()
                .filter(e -> e.getKey().equals(beanName))
                .map(Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    private static List<Entry<String, BeansFeed.Bean>> extractBeansFeed(BeansFeed body) {
        return body.contexts().values().stream()
                .flatMap(context -> context.beans().entrySet().stream())
                .toList();
    }
}
