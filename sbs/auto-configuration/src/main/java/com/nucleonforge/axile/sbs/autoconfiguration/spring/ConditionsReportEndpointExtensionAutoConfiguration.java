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
package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.conditions.ConditionsReportEndpointExtension;

/**
 * Auto-configuration for the {@link ConditionsReportEndpointExtension}.
 *
 * @since 20.10.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = ConditionsReportEndpointAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = ConditionsReportEndpoint.class)
public class ConditionsReportEndpointExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConditionsReportEndpointExtension conditionsReportEndpointExtension(
            ConditionsReportEndpoint conditionsReportEndpoint) {
        return new ConditionsReportEndpointExtension(conditionsReportEndpoint);
    }
}
