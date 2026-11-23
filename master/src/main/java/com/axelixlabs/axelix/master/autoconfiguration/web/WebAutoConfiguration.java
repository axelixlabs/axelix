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

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.internal.InternalApiRestController;
import com.axelixlabs.axelix.master.filter.SpaStaticResourcesServingFilter;

/**
 * Web layer related auto-configuration.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class WebAutoConfiguration {

    /**
     * The path, that serves as the prefix for all <strong>external</strong> web (web!) requests that are coming to the
     * Axelix Master. By "external" we mean from the front-end UI or from OIDC provider and so on.
     */
    public static final String EXTERNAL_API_PATH = "/api/external";

    public static final String INTERNAL_API_PATH = "/api/internal";

    // Docker AOT training starts Master with spring.main.web-application-type=none, so there is no ServletContext.
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SmartInitializingSingleton dispatcherServletMappingValidator(ServletContext servletContext) {
        return () -> {
            // Registrations are complete, but the web server has not started accepting requests yet.
            servletContext.getServletRegistrations().forEach((name, registration) -> {
                Class<?> servletClass =
                        ClassUtils.resolveClassName(registration.getClassName(), servletContext.getClassLoader());
                if (DispatcherServlet.class.isAssignableFrom(servletClass)) {
                    // A custom spring.mvc.servlet.path (e.g. /some-path-here) can move backend endpoints
                    // outside the paths matched by the authentication filters. Reject custom mappings at
                    // startup to prevent exposing unauthenticated endpoints, whether intentional or accidental.
                    Assert.state(
                            Set.copyOf(registration.getMappings()).equals(Set.of("/")),
                            "Unsupported DispatcherServlet mapping for '" + name + "': "
                                    + registration.getMappings()
                                    + ". Axelix requires only the '/' mapping for authentication filters."
                                    + " Remove custom spring.mvc.servlet.path and servlet registration mappings.");
                }
            });
        };
    }

    @Bean
    @ConfigurationProperties(prefix = "axelix.master.web.static-resources")
    public WebProperties webProperties() {
        return new WebProperties();
    }

    @Bean
    public SpaStaticResourcesServingFilter spaStaticResourcesServingFilter(WebProperties webProperties)
            throws IOException {
        return new SpaStaticResourcesServingFilter(webProperties);
    }

    @Bean
    public WebMvcAutoConfiguration webMvcAutoConfiguration() {
        return new WebMvcAutoConfiguration();
    }

    public static class WebMvcAutoConfiguration implements WebMvcConfigurer {

        @Override
        public void configurePathMatch(PathMatchConfigurer configurer) {

            // prefixes for various types of API
            configurer.addPathPrefix(
                    EXTERNAL_API_PATH, HandlerTypePredicate.forAnnotation(ExternalApiRestController.class));
            configurer.addPathPrefix(
                    INTERNAL_API_PATH, HandlerTypePredicate.forAnnotation(InternalApiRestController.class));
        }
    }
}
