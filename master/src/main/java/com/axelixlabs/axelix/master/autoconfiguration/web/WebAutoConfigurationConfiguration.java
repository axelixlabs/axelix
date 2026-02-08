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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axelixlabs.axelix.master.api.SpaBundlesPathResourceResolver;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.internal.InternalApiRestController;

/**
 * Web layer related auto-configuration.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class WebAutoConfigurationConfiguration {

    @ConfigurationProperties(prefix = "axelix.master.web.static-resources")
    public WebProperties webProperties() {
        return new WebProperties();
    }

    @Configuration
    static class WebMvcAutoConfiguration implements WebMvcConfigurer {

        @Autowired
        private WebProperties webProperties;

        @Override
        public void configurePathMatch(PathMatchConfigurer configurer) {

            // prefixes for various types of API
            configurer.addPathPrefix(
                    "/api/external", HandlerTypePredicate.forAnnotation(ExternalApiRestController.class));
            configurer.addPathPrefix(
                    "/api/internal", HandlerTypePredicate.forAnnotation(InternalApiRestController.class));
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            try {
                registry.addResourceHandler("/**")
                        .addResourceLocations(webProperties.getLocation())
                        .resourceChain(true)
                        .addResolver(new SpaBundlesPathResourceResolver(
                                webProperties.getLocation().createRelative("index.html")));
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Unable to find the index.html inside the root directory for the SPA application", e);
            }
        }
    }
}
