/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.autoconfiguration.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Autoconfiguration for security.
 *
 * @since 11.12.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    /**
     * Autoconfiguration for CORS settings.
     */
    @AutoConfiguration
    public static class CorsAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axile.master.cors")
        public CorsProperties corsProperties() {
            return new CorsProperties();
        }

        @Bean
        public CorsFilter corsFilter(CorsProperties corsProperties) {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();

            config.setAllowedOrigins(corsProperties.getAllowedOrigins());
            config.setAllowedMethods(corsProperties.getAllowedMethods());
            config.setAllowedHeaders(corsProperties.getAllowedHeaders());

            config.setAllowCredentials(corsProperties.isAllowCredentials());

            List<String> exposedHeaders = new ArrayList<>(corsProperties.getExposedHeaders());
            exposedHeaders.add(HttpHeaders.SET_COOKIE);
            config.setExposedHeaders(exposedHeaders);

            config.setMaxAge(corsProperties.getMaxAge());

            source.registerCorsConfiguration("/**", config);

            return new CorsFilter(source);
        }
    }
}
