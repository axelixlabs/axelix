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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.util.List;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.annotations.AxelixManagementContextConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.web.AxelixJacksonDecoder;
import com.axelixlabs.axelix.sbs.spring.core.web.AxelixJacksonEncoder;
import com.axelixlabs.axelix.sbs.spring.core.web.AxelixJacksonHttpMessageConverter;

/**
 * Configures isolated Jackson serialization and deserialization for Axelix Actuator endpoints
 * in the context that serves Actuator (main context or separate management port context),
 * without exposing the isolated mapper or converters to the host application context.
 *
 * @author Darshan A S
 */
@AxelixManagementContextConfiguration
public class AxelixJacksonManagementContextConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(
            name = {
                "org.springframework.web.servlet.config.annotation.WebMvcConfigurer",
                "tools.jackson.databind.json.JsonMapper"
            })
    static class ServletAxelixJacksonConfiguration {

        @Bean
        public WebMvcConfigurer axelixEndpointWebMvcConfigurer() {
            AxelixJacksonHttpMessageConverter converter = new AxelixJacksonHttpMessageConverter(
                    JsonMapper.builder().findAndAddModules().build());
            return new WebMvcConfigurer() {
                @Override
                @SuppressWarnings("removal")
                public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                    converters.add(0, converter);
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(
            name = {
                "org.springframework.web.reactive.config.WebFluxConfigurer",
                "tools.jackson.databind.json.JsonMapper"
            })
    static class ReactiveAxelixJacksonConfiguration {

        @Bean
        public WebFluxConfigurer axelixEndpointWebFluxConfigurer() {
            JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
            AxelixJacksonEncoder encoder = new AxelixJacksonEncoder(mapper);
            AxelixJacksonDecoder decoder = new AxelixJacksonDecoder(mapper);
            return new WebFluxConfigurer() {
                @Override
                public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
                    configurer.customCodecs().registerWithDefaultConfig(encoder);
                    configurer.customCodecs().registerWithDefaultConfig(decoder);
                }
            };
        }
    }
}
