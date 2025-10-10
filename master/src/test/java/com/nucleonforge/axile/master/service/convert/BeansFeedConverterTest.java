//package com.nucleonforge.axile.master.service.convert;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.assertj.core.api.InstanceOfAssertFactories;
//import org.junit.jupiter.api.Test;
//
//import com.nucleonforge.axile.common.api.BeansFeed;
//import com.nucleonforge.axile.master.api.response.BeanShortProfile;
//import com.nucleonforge.axile.master.api.response.BeansFeedResponse;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Unit tests for {@link BeansFeedConverter}.
// *
// * @author Mikhail Polivakha
// */
//class BeansFeedConverterTest {
//
//    private final BeansFeedConverter subject = new BeansFeedConverter();
//
//    @Test
//    void testConvertHappyPath() {
//        // when.
//        BeansFeedResponse beansFeedResponse =
//                subject.convertInternal(new BeansFeed(Map.of("main", new BeansFeed.Context("parentId", beansMap()))));
//
//        // then.
//        assertThat(beansFeedResponse).extracting(BeansFeedResponse::getBeans).satisfies(beanShortProfiles -> {
//            assertThat(beanShortProfiles).hasSize(3);
//
//            BeanShortProfile bean1 = getBeanByName(beansFeedResponse, "bean1");
//            assertThat(bean1).extracting(BeanShortProfile::beanName).isEqualTo("bean1");
//            assertThat(bean1).extracting(BeanShortProfile::className).isEqualTo("java.lang.String");
//            assertThat(bean1).extracting(BeanShortProfile::scope).isEqualTo("singleton");
//            assertThat(bean1).extracting(BeanShortProfile::isPrimary).isEqualTo(false);
//            assertThat(bean1).extracting(BeanShortProfile::isLazyInit).isEqualTo(false);
//            assertThat(bean1)
//                    .extracting(BeanShortProfile::qualifiers, InstanceOfAssertFactories.COLLECTION)
//                    .isEmpty();
//            assertThat(bean1)
//                    .extracting(BeanShortProfile::aliases, InstanceOfAssertFactories.COLLECTION)
//                    .hasSize(0);
//            assertThat(bean1)
//                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
//                    .hasSize(0);
//            assertThat(bean1)
//                    .extracting(BeanShortProfile::beanSource)
//                    .isInstanceOf(BeanShortProfile.ComponentVariant.class);
//
//            BeanShortProfile bean2 = getBeanByName(beansFeedResponse, "bean2");
//            assertThat(bean2).extracting(BeanShortProfile::beanName).isEqualTo("bean2");
//            assertThat(bean2).extracting(BeanShortProfile::className).isEqualTo("java.lang.Integer");
//            assertThat(bean2).extracting(BeanShortProfile::scope).isEqualTo("session");
//            assertThat(bean2).extracting(BeanShortProfile::isPrimary).isEqualTo(true);
//            assertThat(bean2).extracting(BeanShortProfile::isLazyInit).isEqualTo(false);
//            assertThat(bean2)
//                    .extracting(BeanShortProfile::qualifiers, InstanceOfAssertFactories.COLLECTION)
//                    .containsOnly("first");
//
//            assertThat(bean2)
//                    .extracting(BeanShortProfile::aliases, InstanceOfAssertFactories.COLLECTION)
//                    .hasSize(0);
//            assertThat(bean2)
//                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
//                    .containsOnly("dep1", "dep2");
//            assertThat(bean2)
//                    .extracting(BeanShortProfile::beanSource)
//                    .isInstanceOf(BeanShortProfile.FactoryBean.class)
//                    .extracting("factoryBeanName")
//                    .isEqualTo("someFactoryBean");
//
//            BeanShortProfile bean3 = getBeanByName(beansFeedResponse, "bean3");
//            assertThat(bean3).extracting(BeanShortProfile::beanName).isEqualTo("bean3");
//            assertThat(bean3).extracting(BeanShortProfile::className).isEqualTo("java.util.Date");
//            assertThat(bean3).extracting(BeanShortProfile::scope).isEqualTo("prototype");
//            assertThat(bean3)
//                    .extracting(BeanShortProfile::aliases, InstanceOfAssertFactories.COLLECTION)
//                    .containsOnly("abc", "bcd");
//            assertThat(bean3)
//                    .extracting(BeanShortProfile::dependencies, InstanceOfAssertFactories.COLLECTION)
//                    .hasSize(0);
//            assertThat(bean3).extracting(BeanShortProfile::isPrimary).isEqualTo(true);
//            assertThat(bean3).extracting(BeanShortProfile::isLazyInit).isEqualTo(true);
//            assertThat(bean3)
//                    .extracting(BeanShortProfile::qualifiers, InstanceOfAssertFactories.COLLECTION)
//                    .containsOnly("one", "two");
//            assertThat(bean3)
//                    .extracting(BeanShortProfile::beanSource)
//                    .isInstanceOf(BeanShortProfile.BeanMethod.class)
//                    .satisfies(beanSource -> {
//                        BeanShortProfile.BeanMethod beanMethod = (BeanShortProfile.BeanMethod) beanSource;
//                        assertThat(beanMethod.enclosingClassName()).isEqualTo("com.example.Configuration");
//                        assertThat(beanMethod.methodName()).isEqualTo("createDate");
//                    });
//        });
//    }
//
//    private static BeanShortProfile getBeanByName(BeansFeedResponse beansFeedResponse, String beanName) {
//        return beansFeedResponse.getBeans().stream()
//                .filter(profile -> profile.beanName().equals(beanName))
//                .findFirst()
//                .get();
//    }
//
//    private static Map<String, BeansFeed.Bean> beansMap() {
//        return Map.of(
//                "bean1",
//                new BeansFeed.Bean(
//                        "singleton", "java.lang.String", Set.of(), Set.of(), false, false, List.of(), null, null, null),
//                "bean2",
//                new BeansFeed.Bean(
//                        "session",
//                        "java.lang.Integer",
//                        Set.of(),
//                        Set.of("dep1", "dep2"),
//                        false,
//                        true,
//                        List.of("first"),
//                        null,
//                        null,
//                        "someFactoryBean"),
//                "bean3",
//                new BeansFeed.Bean(
//                        "prototype",
//                        "java.util.Date",
//                        Set.of("abc", "bcd"),
//                        Set.of(),
//                        true,
//                        true,
//                        List.of("one", "two"),
//                        "com.example.Configuration",
//                        "createDate",
//                        null));
//    }
//}
