package com.nucleonforge.axile.master.service.convert;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.BeansFeed.BeanDependency;
import com.nucleonforge.axile.master.api.response.BeanShortProfile;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.BeanDependencyProfile;
import com.nucleonforge.axile.master.api.response.BeansFeedResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BeansFeedConverter}.
 *
 * @author Mikhail Polivakha
 */
class BeansFeedConverterTest {

    private final BeansFeedConverter subject = new BeansFeedConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        BeansFeedResponse beansFeedResponse =
                subject.convertInternal(new BeansFeed(Map.of("main", new BeansFeed.Context("parentId", beansMap()))));

        // then.
        assertThat(beansFeedResponse).extracting(BeansFeedResponse::getBeans).satisfies(beanShortProfiles -> {
            assertThat(beanShortProfiles).hasSize(3);

            BeanShortProfile bean1 = getBeanByName(beansFeedResponse, "bean1");
            assertThat(bean1).extracting(BeanShortProfile::beanName).isEqualTo("bean1");
            assertThat(bean1).extracting(BeanShortProfile::className).isEqualTo("java.lang.String");
            assertThat(bean1).extracting(BeanShortProfile::scope).isEqualTo("singleton");
            assertThat(bean1).extracting(BeanShortProfile::isPrimary).isEqualTo(false);
            assertThat(bean1).extracting(BeanShortProfile::isLazyInit).isEqualTo(false);
            assertThat(bean1).extracting(BeanShortProfile::isConfigPropsBean).isEqualTo(true);
            assertThat(bean1)
                    .extracting(BeanShortProfile::qualifiers, InstanceOfAssertFactories.COLLECTION)
                    .isEmpty();
            assertThat(bean1)
                    .extracting(BeanShortProfile::aliases, InstanceOfAssertFactories.COLLECTION)
                    .hasSize(0);
            assertThat(bean1)
                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
                    .hasSize(0);
            assertThat(bean1)
                    .extracting(BeanShortProfile::beanSource)
                    .isInstanceOf(BeanShortProfile.ComponentVariant.class);

            BeanShortProfile bean2 = getBeanByName(beansFeedResponse, "bean2");
            assertThat(bean2).extracting(BeanShortProfile::beanName).isEqualTo("bean2");
            assertThat(bean2).extracting(BeanShortProfile::className).isEqualTo("java.lang.Integer");
            assertThat(bean2).extracting(BeanShortProfile::scope).isEqualTo("session");
            assertThat(bean2).extracting(BeanShortProfile::isPrimary).isEqualTo(true);
            assertThat(bean2).extracting(BeanShortProfile::isLazyInit).isEqualTo(false);
            assertThat(bean2).extracting(BeanShortProfile::isConfigPropsBean).isEqualTo(false);
            assertThat(bean2)
                    .extracting(BeanShortProfile::qualifiers, InstanceOfAssertFactories.COLLECTION)
                    .containsOnly("first");
            assertThat(bean2)
                    .extracting(BeanShortProfile::aliases, InstanceOfAssertFactories.COLLECTION)
                    .hasSize(0);
            assertThat(bean2)
                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
                    .hasSize(1)
                    .extracting("name")
                    .containsOnly("org.springframework.boot.autoconfigure.orm.jpa.JpaProperties");
            assertThat(bean2)
                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
                    .extracting("isConfigPropsDependency")
                    .containsOnly(true);
            assertThat(bean2)
                    .extracting(BeanShortProfile::beanSource)
                    .isInstanceOf(BeanShortProfile.FactoryBean.class)
                    .extracting("factoryBeanName")
                    .isEqualTo("someFactoryBean");

            BeanShortProfile bean3 = getBeanByName(beansFeedResponse, "bean3");
            assertThat(bean3).extracting(BeanShortProfile::beanName).isEqualTo("bean3");
            assertThat(bean3).extracting(BeanShortProfile::className).isEqualTo("java.util.Date");
            assertThat(bean3).extracting(BeanShortProfile::scope).isEqualTo("prototype");
            assertThat(bean3)
                    .extracting(BeanShortProfile::aliases, InstanceOfAssertFactories.COLLECTION)
                    .containsOnly("abc", "bcd");
            assertThat(bean3)
                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
                    .filteredOn(dep -> "org.springframework.boot.autoconfigure.orm.jpa.JpaProperties"
                            .equals(((BeanDependencyProfile) dep).name()))
                    .extracting("isConfigPropsDependency")
                    .containsOnly(true);
            assertThat(bean3)
                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
                    .filteredOn(dep ->
                            "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration"
                                    .equals(((BeanDependencyProfile) dep).name()))
                    .extracting("isConfigPropsDependency")
                    .containsOnly(false);
            assertThat(bean3).extracting(BeanShortProfile::isPrimary).isEqualTo(true);
            assertThat(bean3).extracting(BeanShortProfile::isLazyInit).isEqualTo(true);
            assertThat(bean3).extracting(BeanShortProfile::isConfigPropsBean).isEqualTo(true);
            assertThat(bean3)
                    .extracting(BeanShortProfile::qualifiers, InstanceOfAssertFactories.COLLECTION)
                    .containsOnly("one", "two");
            assertThat(bean3)
                    .extracting(BeanShortProfile::beanSource)
                    .isInstanceOf(BeanShortProfile.BeanMethod.class)
                    .satisfies(beanSource -> {
                        BeanShortProfile.BeanMethod beanMethod = (BeanShortProfile.BeanMethod) beanSource;
                        assertThat(beanMethod.enclosingClassName()).isEqualTo("enclosingClass");
                        assertThat(beanMethod.enclosingClassFullName())
                                .isEqualTo("org.springframework.boot.enclosingClass");
                        assertThat(beanMethod.methodName()).isEqualTo("factoryMethod");
                    });
        });
    }

    private static BeanShortProfile getBeanByName(BeansFeedResponse beansFeedResponse, String beanName) {
        return beansFeedResponse.getBeans().stream()
                .filter(profile -> profile.beanName().equals(beanName))
                .findFirst()
                .get();
    }

    private static Map<String, BeansFeed.Bean> beansMap() {
        return Map.of(

                // first bean
                "some.prefix-bean1",
                new BeansFeed.Bean(
                        "singleton",
                        "java.lang.String",
                        BeansFeed.ProxyType.CGLIB,
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        true,
                        List.of(),
                        new BeansFeed.ComponentVariant()),

                // second bean
                "bean2",
                new BeansFeed.Bean(
                        "session",
                        "java.lang.Integer",
                        BeansFeed.ProxyType.JDK_PROXY,
                        Set.of(),
                        Set.of(new BeanDependency(
                                "spring.jpa-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties", true)),
                        false,
                        true,
                        false,
                        List.of("first"),
                        new BeansFeed.FactoryBean("someFactoryBean")),

                // third bean
                "bean3",
                new BeansFeed.Bean(
                        "prototype",
                        "java.util.Date",
                        BeansFeed.ProxyType.NO_PROXYING,
                        Set.of("abc", "bcd"),
                        Set.of(
                                new BeanDependency(
                                        "spring.jpa-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties",
                                        true),
                                new BeanDependency(
                                        "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration",
                                        false)),
                        true,
                        true,
                        true,
                        List.of("one", "two"),
                        new BeansFeed.BeanMethod(
                                "enclosingClass", "org.springframework.boot.enclosingClass", "factoryMethod")));
    }
}
