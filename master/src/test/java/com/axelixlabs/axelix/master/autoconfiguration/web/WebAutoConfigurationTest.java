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
package com.axelixlabs.axelix.master.autoconfiguration.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.DispatcherServlet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link WebAutoConfiguration}, verifying that Master starts with the default
 * DispatcherServlet mapping and rejects custom servlet paths or additional programmatic mappings.
 *
 * @author Nikita Kirillov
 */
class WebAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner(
                    AnnotationConfigServletWebServerApplicationContext::new)
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withPropertyValues("axelix.master.web.static-resources.location=classpath:/spa/")
            .withUserConfiguration(
                    ServerConfiguration.class, DispatcherServletAutoConfiguration.class, WebAutoConfiguration.class);

    @Test
    void startsWithoutServletContext() {
        // given.
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class, WebAutoConfiguration.class))
                .withPropertyValues("axelix.master.web.static-resources.location=classpath:/spa/");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
        });
    }

    @Test
    void startsWithDefaultServletMapping() {
        // given / when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
        });
    }

    @Test
    void startsWithContextPathAndDefaultServletMapping() {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues("server.servlet.context-path=/axelix");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
        });
    }

    @Test
    void rejectsCustomServletPath() {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues("spring.mvc.servlet.path=/api");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).isInstanceOf(IllegalStateException.class);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"/custom/*", "/*"})
    void rejectsAdditionalProgrammaticServletMapping(String mapping) {
        // given.
        ApplicationContextRunner runner = contextRunner
                .withUserConfiguration(AdditionalMappingConfiguration.class)
                .withPropertyValues("test.additional-mapping=" + mapping);

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).isInstanceOf(IllegalStateException.class);
        });
    }

    @Test
    void rejectsDispatcherServletSubclassInsideRegistrationBean() {
        // given.
        ApplicationContextRunner runner = contextRunner.withUserConfiguration(SubclassServletConfiguration.class);

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).isInstanceOf(IllegalStateException.class);
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SubclassServletConfiguration {
        @Bean
        ServletRegistrationBean<CustomDispatcherServlet> customServletRegistration() {
            return new ServletRegistrationBean<>(new CustomDispatcherServlet(), "/*");
        }
    }

    static class CustomDispatcherServlet extends DispatcherServlet {}

    @TestConfiguration(proxyBeanMethods = false)
    static class ServerConfiguration {
        @Bean
        TomcatServletWebServerFactory servletWebServerFactory(Environment environment) {
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(0);
            factory.setContextPath(environment.getProperty("server.servlet.context-path", ""));
            return factory;
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class AdditionalMappingConfiguration {
        @Bean
        ServletContextInitializer additionalServletMapping(Environment environment) {
            return servletContext -> servletContext
                    .getServletRegistration("dispatcherServlet")
                    .addMapping(environment.getRequiredProperty("test.additional-mapping"));
        }
    }
}
