package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.BeansFeed.Bean;

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
                  "beans" : {
                    "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration" : {
                      "aliases" : [ "abc", "bcd" ],
                      "scope" : "singleton",
                      "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration",
                      "dependencies" : [ ]
                    },
                    "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration" : {
                      "aliases" : [ ],
                      "scope" : "singleton",
                      "type" : "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
                      "dependencies" : [ "123", "321" ]
                    },
                    "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration" : {
                      "aliases" : [ ],
                      "scope" : "singleton",
                      "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
                      "dependencies" : [ ]
                    }
                  }
                }
              }
            }
            """;

        BeansFeed beansFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(beansFeed.getContexts()).hasEntrySatisfying("application", context -> {
            assertThat(context.getBeans()).hasSize(3);

            Bean first = context.getBeans()
                    .get(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");
            assertThat(first.getAliases()).containsOnly("abc", "bcd");
            assertThat(first.getDependencies()).isEmpty();
            assertThat(first.getScope()).isEqualTo("singleton");
            assertThat(first.getType())
                    .isEqualTo(
                            "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration");

            Bean second = context.getBeans()
                    .get("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");
            assertThat(second.getAliases()).isEmpty();
            assertThat(second.getDependencies()).containsOnly("123", "321");
            assertThat(second.getScope()).isEqualTo("singleton");
            assertThat(second.getType())
                    .isEqualTo("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration");

            Bean third = context.getBeans()
                    .get("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
            assertThat(third.getAliases()).isEmpty();
            assertThat(third.getDependencies()).isEmpty();
            assertThat(third.getScope()).isEqualTo("singleton");
            assertThat(third.getType())
                    .isEqualTo("org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration");
        });
    }
}
