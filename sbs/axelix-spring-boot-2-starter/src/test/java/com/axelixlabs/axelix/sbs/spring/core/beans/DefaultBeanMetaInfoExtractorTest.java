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

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.ComponentVariant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.ANONYMOUS_BEAN;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.CONFIGURATION_BEAN;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.CUSTOM_DATABASE_QUALIFIER_BEAN;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.LAZY_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.LAZY_PRIMARY_BEAN_METHOD;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.PRIMARY_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.QUALIFIED_BEAN_METHOD;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.QUALIFIED_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.REGULAR_COMPONENT;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.SPRING_DATA_REPOSITORY;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.STATIC_BFPP_BEAN;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.SYNTHETIC_BEAN_DEFINITION;
import static com.axelixlabs.axelix.sbs.spring.core.beans.AbstractBeansIntegrationTest.BeansTestConfiguration.TRANSACTIONAL_BEAN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultBeanMetaInfoExtractor}.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class DefaultBeanMetaInfoExtractorTest extends AbstractBeansIntegrationTest {

    @Autowired
    private BeanMetaInfoExtractor metaInfoExtractor;

    @Autowired
    private ConfigurableListableBeanFactory testBeanFactory;

    @Test
    void shouldExtractForSimpleServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(REGULAR_COMPONENT, testBeanFactory);

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
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(CacheAutoConfiguration.class.getName(), testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getAutoConfigurationRef()).isEqualTo("CacheAutoConfiguration");
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
        });
    }

    @Test
    void shouldExtractForAutoConfigurationBeanMethod() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract("cacheManagerCustomizers", testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getAutoConfigurationRef()).isEqualTo("CacheAutoConfiguration#cacheManagerCustomizers");
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat((BeansFeed.BeanMethod) it.getBeanSource()).satisfies(beanMethod -> {
                assertThat(beanMethod.getEnclosingClassFullName()).isEqualTo(CacheAutoConfiguration.class.getName());
                assertThat(beanMethod.getMethodName()).isEqualTo("cacheManagerCustomizers");
            });
        });
    }

    @Test
    void shouldExtractLazyServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(LAZY_COMPONENT, testBeanFactory);

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
                metaInfoExtractor.extract(PRIMARY_COMPONENT, testBeanFactory);

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
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(QUALIFIED_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getQualifiers()).contains(QUALIFIED_COMPONENT);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractLazyPrimaryBeanMethod() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(LAZY_PRIMARY_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeansFeed.BeanMethod) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getEnclosingClassName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getSimpleName());
                assertThat(bs.getEnclosingClassFullName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getName());
                assertThat(bs.getMethodName()).isEqualTo("lazyPrimaryBean");
            });
        });
    }

    @Test
    void shouldExtractQualifiedBeanMethod() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(QUALIFIED_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).contains(QUALIFIED_BEAN_METHOD);
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeansFeed.BeanMethod) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getEnclosingClassName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getSimpleName());
                assertThat(bs.getEnclosingClassFullName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getName());
                assertThat(bs.getMethodName()).isEqualTo(QUALIFIED_BEAN_METHOD);
            });
        });
    }

    @Test
    void shouldExtractSpringDataRepositoryBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(SPRING_DATA_REPOSITORY, testBeanFactory);

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
                CUSTOM_DATABASE_QUALIFIER_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).contains(CUSTOM_DATABASE_QUALIFIER_BEAN);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractConfigurationBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(CONFIGURATION_BEAN, testBeanFactory);

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
                metaInfoExtractor.extract(TRANSACTIONAL_BEAN, testBeanFactory);

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
                metaInfoExtractor.extract(ANONYMOUS_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();

            BeansFeed.BeanMethod source = (BeansFeed.BeanMethod) it.getBeanSource();
            assertThat(source.getEnclosingClassName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getSimpleName());
            assertThat(source.getEnclosingClassFullName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getName());
            assertThat(source.getMethodName()).isEqualTo(ANONYMOUS_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForStaticBeanFactoryPostProcessorBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(STATIC_BFPP_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();

            BeansFeed.BeanMethod source = (BeansFeed.BeanMethod) it.getBeanSource();
            assertThat(source.getEnclosingClassName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getSimpleName());
            assertThat(source.getEnclosingClassFullName()).isEqualTo(AbstractBeansIntegrationTest.BeansTestConfiguration.class.getName());
            assertThat(source.getMethodName()).isEqualTo(STATIC_BFPP_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForSyntheticallyGeneratedBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(SYNTHETIC_BEAN_DEFINITION, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.getProxyType()).isEqualTo(BeansFeed.ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getBeanSource()).isInstanceOf(BeansFeed.SyntheticBean.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(CUSTOM_DATABASE_QUALIFIER_BEAN)
    public @interface CustomDatabaseQualifier {}
}
