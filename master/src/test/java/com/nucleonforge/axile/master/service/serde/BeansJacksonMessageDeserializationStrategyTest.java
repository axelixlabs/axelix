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
                  "beanSource": {
                     "origin": "COMPONENT_ANNOTATION"
                  }
                },
                "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration" : {
                  "aliases" : [ ],
                  "scope" : "singleton",
                  "type" : "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
                  "dependencies" : [ "123", "321" ],
                  "isLazyInit" : true,
                  "isPrimary" : false,
                  "qualifiers" : [ ],
                  "beanSource": {
                    "enclosingClassName": "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration",
                    "methodName": "entityManagerFactoryBuilder",
                    "origin": "BEAN_METHOD"
                  }
                },
                "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration" : {
                  "aliases" : [ ],
                  "scope" : "singleton",
                  "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
                  "dependencies" : [ ],
                  "isLazyInit" : false,
                  "isPrimary" : false,
                  "qualifiers" : [ "main", "secondary" ],
                  "beanSource": {
                    "factoryBeanName": "org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean",
                    "origin": "FACTORY_BEAN"
                  }
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
        });
    }
}
