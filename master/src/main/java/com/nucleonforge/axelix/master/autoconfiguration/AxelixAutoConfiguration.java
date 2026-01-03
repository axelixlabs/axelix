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
package com.nucleonforge.axelix.master.autoconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.domain.AxelixVersionDiscoverer;
import com.nucleonforge.axelix.common.domain.PropertiesAxelixVersionDiscoverer;
import com.nucleonforge.axelix.master.api.error.handle.ApiExceptionTranslator;
import com.nucleonforge.axelix.master.exception.ExceptionHandlingFilter;

/**
 * General Auto-configuration of Axelix project.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class AxelixAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixVersionDiscoverer axelixVersionDiscoverer() {
        return new PropertiesAxelixVersionDiscoverer("META-INF/axelix.properties");
    }

    @Bean
    public ExceptionHandlingFilter exceptionHandlingFilter(
            ApiExceptionTranslator apiExceptionTranslator, ObjectMapper objectMapper) {
        return new ExceptionHandlingFilter(apiExceptionTranslator, objectMapper);
    }
}
