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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.ComponentVariant;
import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultBeanMetaInfoExtractor}.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class DefaultBeanMetaInfoExtractorTest extends AbstractEndpointTest {

    @Autowired
    private BeanMetaInfoExtractor metaInfoExtractor;

    @Autowired
    private ConfigurableListableBeanFactory testBeanFactory;

    @Test
    void shouldExtractForSimpleServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.REGULAR_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractForAutoConfigurationBeanClass() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(HttpMessageConvertersAutoConfiguration.class.getName(), testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getAutoConfigurationRef()).isEqualTo("HttpMessageConvertersAutoConfiguration");
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
        });
    }

    @Test
    void shouldExtractForAutoConfigurationBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract("messageConverters", testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getAutoConfigurationRef())
                    .isEqualTo("HttpMessageConvertersAutoConfiguration#messageConverters");
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) it.getBeanSource()).satisfies(beanMethod -> {
                assertThat(beanMethod.getEnclosingClassFullName())
                        .isEqualTo(HttpMessageConvertersAutoConfiguration.class.getName());
                assertThat(beanMethod.getMethodName()).isEqualTo("messageConverters");
            });
        });
    }

    @Test
    void shouldExtractLazyServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.LAZY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractPrimaryComponentBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.PRIMARY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractQualifiedServiceBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanMetaInfoExtractorTestFixtures.QUALIFIED_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getQualifiers()).contains(DefaultBeanMetaInfoExtractorTestFixtures.QUALIFIED_COMPONENT);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractLazyPrimaryBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanMetaInfoExtractorTestFixtures.LAZY_PRIMARY_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeansFeed.BeanMethod) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getEnclosingClassName())
                        .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getSimpleName());
                assertThat(bs.getEnclosingClassFullName())
                        .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getName());
                assertThat(bs.getMethodName()).isEqualTo("lazyPrimaryBean");
            });
        });
    }

    @Test
    void shouldExtractQualifiedBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanMetaInfoExtractorTestFixtures.QUALIFIED_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).contains(DefaultBeanMetaInfoExtractorTestFixtures.QUALIFIED_BEAN_METHOD);
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeansFeed.BeanMethod) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getEnclosingClassName())
                        .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getSimpleName());
                assertThat(bs.getEnclosingClassFullName())
                        .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getName());
                assertThat(bs.getMethodName())
                        .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.QUALIFIED_BEAN_METHOD);
            });
        });
    }

    @Test
    void shouldExtractSpringDataRepositoryBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanMetaInfoExtractorTestFixtures.SPRING_DATA_REPOSITORY, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.JDK_PROXY);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.FactoryBean.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeansFeed.FactoryBean) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getFactoryBeanName()).isEqualTo(JpaRepositoryFactoryBean.class.getName());
            });
        });
    }

    @Test
    void shouldExtractCustomQualifierAnnotations() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanMetaInfoExtractorTestFixtures.CUSTOM_DATABASE_QUALIFIER_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers())
                    .contains(DefaultBeanMetaInfoExtractorTestFixtures.CUSTOM_DATABASE_QUALIFIER_BEAN);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractConfigurationBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.CONFIGURATION_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.CGLIB);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractTransactionalBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.TRANSACTIONAL_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.CGLIB);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractBeanFromAnonymousClass() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.ANONYMOUS_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();

            BeansFeed.BeanMethod source = (BeansFeed.BeanMethod) it.getBeanSource();
            assertThat(source.getEnclosingClassName())
                    .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getSimpleName());
            assertThat(source.getEnclosingClassFullName())
                    .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getName());
            assertThat(source.getMethodName()).isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.ANONYMOUS_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForStaticBeanFactoryPostProcessorBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanMetaInfoExtractorTestFixtures.STATIC_BFPP_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();

            BeansFeed.BeanMethod source = (BeansFeed.BeanMethod) it.getBeanSource();
            assertThat(source.getEnclosingClassName())
                    .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getSimpleName());
            assertThat(source.getEnclosingClassFullName())
                    .isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.class.getName());
            assertThat(source.getMethodName()).isEqualTo(DefaultBeanMetaInfoExtractorTestFixtures.STATIC_BFPP_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForSyntheticallyGeneratedBean() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanMetaInfoExtractorTestFixtures.SYNTHETIC_BEAN_DEFINITION, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.SyntheticBean.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }
}
