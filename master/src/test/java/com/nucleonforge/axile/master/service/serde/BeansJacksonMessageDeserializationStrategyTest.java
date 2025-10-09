package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.BeansFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BeansJacksonMessageDeserializationStrategy}. The json for deserialization was taken from
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/beans.html">official doc.</a>.
 *
 * @author Mikhail Polivakha
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
                  "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration",
                  "dependencies" : [ ],
                  "isLazyInit" : false,
                  "isPrimary" : true,
                  "qualifiers" : [ "qualifier1" ],
                  "enclosingClassName" : "com.example.Config",
                  "methodName" : "dispatcherServletRegistration",
                  "factoryBeanName" : null
                },
                "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration" : {
                  "aliases" : [ ],
                  "scope" : "singleton",
                  "type" : "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
                  "dependencies" : [ "123", "321" ],
                  "isLazyInit" : true,
                  "isPrimary" : false,
                  "qualifiers" : [ ],
                  "enclosingClassName" : null,
                  "methodName" : null,
                  "factoryBeanName" : "propertyPlaceholderFactory"
                },
                "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration" : {
                  "aliases" : [ ],
                  "scope" : "singleton",
                  "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
                  "dependencies" : [ ],
                  "isLazyInit" : false,
                  "isPrimary" : false,
                  "qualifiers" : [ "main", "secondary" ],
                  "enclosingClassName" : null,
                  "methodName" : null,
                  "factoryBeanName" : null
                }
              }
            }
          }
        }
        """;

        BeansFeed beansFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(beansFeed.contexts()).hasEntrySatisfying("application", context -> {
            assertThat(context.parentId()).isEqualTo("parentContext");
            assertThat(context.beans()).hasSize(3);

            BeansFeed.Bean first = context.beans()
                    .get(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.aliases()).containsOnly("abc", "bcd");
            assertThat(first.dependencies()).isEmpty();
            assertThat(first.scope()).isEqualTo("singleton");
            assertThat(first.type())
                    .isEqualTo(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.isLazyInit()).isFalse();
            assertThat(first.isPrimary()).isTrue();
            assertThat(first.qualifiers()).containsOnly("qualifier1");
            assertThat(first.enclosingClassName()).isEqualTo("com.example.Config");
            assertThat(first.methodName()).isEqualTo("dispatcherServletRegistration");
            assertThat(first.factoryBeanName()).isNull();

            BeansFeed.Bean second = context.beans()
                    .get("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.aliases()).isEmpty();
            assertThat(second.dependencies()).containsOnly("123", "321");
            assertThat(second.scope()).isEqualTo("singleton");
            assertThat(second.type())
                    .isEqualTo("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.isLazyInit()).isTrue();
            assertThat(second.isPrimary()).isFalse();
            assertThat(second.qualifiers()).isEmpty();
            assertThat(second.enclosingClassName()).isNull();
            assertThat(second.methodName()).isNull();
            assertThat(second.factoryBeanName()).isEqualTo("propertyPlaceholderFactory");

            BeansFeed.Bean third = context.beans()
                    .get("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.aliases()).isEmpty();
            assertThat(third.dependencies()).isEmpty();
            assertThat(third.scope()).isEqualTo("singleton");
            assertThat(third.type())
                    .isEqualTo("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.isLazyInit()).isFalse();
            assertThat(third.isPrimary()).isFalse();
            assertThat(third.qualifiers()).containsOnly("main", "secondary");
            assertThat(third.enclosingClassName()).isNull();
            assertThat(third.methodName()).isNull();
            assertThat(third.factoryBeanName()).isNull();
        });
    }
}
