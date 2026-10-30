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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import com.axelixlabs.axelix.sbs.spring.core.contract.beans.BeanMethod;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.ComponentVariant;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.FactoryBean;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.ProxyType;
import com.axelixlabs.axelix.sbs.spring.core.contract.beans.SyntheticBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link DefaultBeanMetaInfoExtractor}.
 *
 * <p>The bean fixtures it exercises are declared by
 * {@link AbstractBeansSharedContextTest.DefaultBeanAnalyzerTestConfig} on the shared parent context.
 *
 * @since 07.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Andrey Kotov
 * @author Vyacheslav Yanin
 */
class DefaultBeanMetaInfoExtractorTest extends AbstractBeansSharedContextTest {

    private static final String INACTIVE_SCOPE = "inactive";

    private static final String INACTIVE_SCOPED_TARGET = "scopedTarget.inactiveScopedBean";

    @Autowired
    private BeanMetaInfoExtractor metaInfoExtractor;

    @Autowired
    private ConfigurableListableBeanFactory testBeanFactory;

    @Test
    void shouldExtractForSimpleServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.REGULAR_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
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
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
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
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getAutoConfigurationRef()).isEqualTo("CacheAutoConfiguration#cacheManagerCustomizers");
            assertThat(it.getBeanSource()).isInstanceOf(BeanMethod.class);
            assertThat((BeanMethod) it.getBeanSource()).satisfies(beanMethod -> {
                assertThat(beanMethod.getEnclosingClassFullName()).isEqualTo(CacheAutoConfiguration.class.getName());
                assertThat(beanMethod.getMethodName()).isEqualTo("cacheManagerCustomizers");
            });
        });
    }

    @Test
    void shouldExtractLazyServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.LAZY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.UNKNOWN);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractPrimaryComponentBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.PRIMARY_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractQualifiedServiceBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.QUALIFIED_COMPONENT, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).contains(DefaultBeanAnalyzerTestConfig.QUALIFIED_COMPONENT);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractLazyPrimaryBeanMethod() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.LAZY_PRIMARY_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isTrue();
            assertThat(it.isPrimary()).isTrue();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.UNKNOWN);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeanMethod) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getEnclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
                assertThat(bs.getEnclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
                assertThat(bs.getMethodName()).isEqualTo("lazyPrimaryBean");
            });
        });
    }

    @Test
    void shouldExtractQualifiedBeanMethod() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.QUALIFIED_BEAN_METHOD, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).contains(DefaultBeanAnalyzerTestConfig.QUALIFIED_BEAN_METHOD);
            assertThat(it.getBeanSource()).isInstanceOf(BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((BeanMethod) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getEnclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
                assertThat(bs.getEnclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
                assertThat(bs.getMethodName()).isEqualTo(DefaultBeanAnalyzerTestConfig.QUALIFIED_BEAN_METHOD);
            });
        });
    }

    @Test
    void shouldExtractSpringDataRepositoryBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.SPRING_DATA_REPOSITORY, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.UNKNOWN);
            assertThat(it.getQualifiers()).isEmpty();
            assertThat(it.getBeanSource()).isInstanceOf(FactoryBean.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
            assertThat((FactoryBean) it.getBeanSource()).satisfies(bs -> {
                assertThat(bs.getFactoryBeanName()).isEqualTo(JpaRepositoryFactoryBean.class.getName());
            });
        });
    }

    @Test
    void shouldExtractCustomQualifierAnnotations() {
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract(
                DefaultBeanAnalyzerTestConfig.CUSTOM_DATABASE_QUALIFIER_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.getQualifiers()).contains(DefaultBeanAnalyzerTestConfig.CUSTOM_DATABASE_QUALIFIER_BEAN);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractConfigurationBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.CONFIGURATION_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.CGLIB);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractTransactionalBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.TRANSACTIONAL_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.CGLIB);
            assertThat(it.getBeanSource()).isInstanceOf(ComponentVariant.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test
    void shouldExtractBeanFromAnonymousClass() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.ANONYMOUS_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.getBeanSource()).isInstanceOf(BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();

            BeanMethod source = (BeanMethod) it.getBeanSource();
            assertThat(source.getEnclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
            assertThat(source.getEnclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
            assertThat(source.getMethodName()).isEqualTo(DefaultBeanAnalyzerTestConfig.ANONYMOUS_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForStaticBeanFactoryPostProcessorBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.STATIC_BFPP_BEAN, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getBeanSource()).isInstanceOf(BeanMethod.class);
            assertThat(it.getAutoConfigurationRef()).isNull();

            BeanMethod source = (BeanMethod) it.getBeanSource();
            assertThat(source.getEnclosingClassName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getSimpleName());
            assertThat(source.getEnclosingClassFullName()).isEqualTo(DefaultBeanAnalyzerTestConfig.class.getName());
            assertThat(source.getMethodName()).isEqualTo(DefaultBeanAnalyzerTestConfig.STATIC_BFPP_BEAN);
        });
    }

    @Test
    void shouldExtractInfoForSyntheticallyGeneratedBean() {
        BeanMetaInfo beanMetaInfo =
                metaInfoExtractor.extract(DefaultBeanAnalyzerTestConfig.SYNTHETIC_BEAN_DEFINITION, testBeanFactory);

        assertThat(beanMetaInfo).satisfies(it -> {
            assertThat(it.getProxyType()).isEqualTo(ProxyType.NO_PROXYING);
            assertThat(it.isLazyInit()).isFalse();
            assertThat(it.isPrimary()).isFalse();
            assertThat(it.getBeanSource()).isInstanceOf(SyntheticBean.class);
            assertThat(it.getAutoConfigurationRef()).isNull();
        });
    }

    @Test // GH-1800
    void shouldExtractMetadataWithoutAccessingInactiveScopedBean() {
        // given.
        DefaultListableBeanFactory isolatedBeanFactory = new DefaultListableBeanFactory();
        Scope inactiveScope = mock(Scope.class);
        when(inactiveScope.get(anyString(), any())).thenThrow(new IllegalStateException("Scope is not active"));

        isolatedBeanFactory.registerScope(INACTIVE_SCOPE, inactiveScope);

        RootBeanDefinition beanDefinition = new RootBeanDefinition(NonInstantiatedBean.class);
        beanDefinition.setScope(INACTIVE_SCOPE);
        isolatedBeanFactory.registerBeanDefinition(INACTIVE_SCOPED_TARGET, beanDefinition);

        // when.
        metaInfoExtractor.extract(INACTIVE_SCOPED_TARGET, isolatedBeanFactory);

        // then.
        verifyNoInteractions(inactiveScope);
    }

    @Test // GH-1800
    void shouldExtractMetadataWithoutInstantiatingLazyBean() {
        // given.
        DefaultListableBeanFactory isolatedBeanFactory = new DefaultListableBeanFactory();
        AtomicBoolean beanInstantiated = new AtomicBoolean();

        RootBeanDefinition beanDefinition = new RootBeanDefinition(NonInstantiatedBean.class);
        beanDefinition.setLazyInit(true);
        beanDefinition.setInstanceSupplier(() -> {
            beanInstantiated.set(true);
            return new NonInstantiatedBean();
        });
        isolatedBeanFactory.registerBeanDefinition("lazyBean", beanDefinition);

        // when.
        metaInfoExtractor.extract("lazyBean", isolatedBeanFactory);

        // then.
        assertThat(beanInstantiated.get()).isFalse();
    }

    @Test // GH-1800
    void shouldExtractMetadataWithoutInstantiatingPrototypeBean() {
        // given.
        DefaultListableBeanFactory isolatedBeanFactory = new DefaultListableBeanFactory();
        AtomicBoolean beanInstantiated = new AtomicBoolean();

        RootBeanDefinition beanDefinition = new RootBeanDefinition(NonInstantiatedBean.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanDefinition.setInstanceSupplier(() -> {
            beanInstantiated.set(true);
            return new NonInstantiatedBean();
        });
        isolatedBeanFactory.registerBeanDefinition("prototypeBean", beanDefinition);

        // when.
        metaInfoExtractor.extract("prototypeBean", isolatedBeanFactory);

        // then.
        assertThat(beanInstantiated.get()).isFalse();
    }

    @Test // GH-1800
    void shouldExtractMetadataWhenBeanTypeCannotBeDeterminedWithoutInitialization() {
        // given.
        DefaultListableBeanFactory isolatedBeanFactory = new DefaultListableBeanFactory();
        AtomicBoolean factoryInstantiated = new AtomicBoolean();
        org.springframework.beans.factory.FactoryBean<?> factoryBean =
                mock(org.springframework.beans.factory.FactoryBean.class);

        RootBeanDefinition beanDefinition = new RootBeanDefinition(org.springframework.beans.factory.FactoryBean.class);
        beanDefinition.setLazyInit(true);
        beanDefinition.setPrimary(true);
        beanDefinition.setInstanceSupplier(() -> {
            factoryInstantiated.set(true);
            return factoryBean;
        });
        isolatedBeanFactory.registerBeanDefinition("unknownTypeBean", beanDefinition);

        // when.
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract("unknownTypeBean", isolatedBeanFactory);

        // then.
        assertThat(beanMetaInfo.getProxyType()).isEqualTo(ProxyType.UNKNOWN);
        assertThat(beanMetaInfo.isLazyInit()).isTrue();
        assertThat(beanMetaInfo.isPrimary()).isTrue();
        assertThat(beanMetaInfo.getAutoConfigurationRef()).isNull();
        assertThat(factoryInstantiated.get()).isFalse();
        verifyNoInteractions(factoryBean);
    }

    @Test // GH-1810
    void shouldNotInstantiateFactoryBeanProductWhenFactoryIsAlreadyInitialized() {
        // given.
        DefaultListableBeanFactory isolatedBeanFactory = new DefaultListableBeanFactory();
        AtomicBoolean productCreated = new AtomicBoolean();

        RootBeanDefinition beanDefinition = new RootBeanDefinition(CountingFactoryBean.class);
        beanDefinition.setInstanceSupplier(() -> new CountingFactoryBean(productCreated));
        isolatedBeanFactory.registerBeanDefinition("factoryBean", beanDefinition);
        isolatedBeanFactory.getBean("&factoryBean");

        // when.
        BeanMetaInfo beanMetaInfo = metaInfoExtractor.extract("factoryBean", isolatedBeanFactory);

        // then.
        assertThat(beanMetaInfo.getProxyType()).isEqualTo(ProxyType.UNKNOWN);
        assertThat(productCreated.get()).isFalse();
    }

    private static class NonInstantiatedBean {}

    private static class CountingFactoryBean implements org.springframework.beans.factory.FactoryBean<Object> {

        private final AtomicBoolean productCreated;

        private CountingFactoryBean(AtomicBoolean productCreated) {
            this.productCreated = productCreated;
        }

        @Override
        public Object getObject() {
            productCreated.set(true);
            return new Object();
        }

        @Override
        public Class<?> getObjectType() {
            return Object.class;
        }
    }
}
