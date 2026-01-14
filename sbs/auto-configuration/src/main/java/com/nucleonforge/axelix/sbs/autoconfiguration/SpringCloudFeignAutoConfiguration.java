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
package com.nucleonforge.axelix.sbs.autoconfiguration;

import feign.Feign;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.sbs.spring.integrations.IntegrationComponentDiscoverer;
import com.nucleonforge.axelix.sbs.spring.integrations.http.FeignClientIntegrationDiscoverer;
import com.nucleonforge.axelix.sbs.spring.integrations.http.HttpIntegration;

/**
 * Auto-configuration for discovering HTTP integrations based on Spring Cloud OpenFeign.
 * <p>
 * Registers a {@link FeignClientIntegrationDiscoverer} if Feign is present on the classpath.
 * </p>
 *
 * @author Nikita Kirillov
 * @since 09.07.2025
 */
@AutoConfiguration
@ConditionalOnClass({Feign.class, FeignClient.class})
public class SpringCloudFeignAutoConfiguration {

    @Bean
    public IntegrationComponentDiscoverer<HttpIntegration> feignClientIntegrationDiscoverer(
            ApplicationContext context) {
        return new FeignClientIntegrationDiscoverer(context);
    }
}
