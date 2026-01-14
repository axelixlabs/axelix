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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.sbs.spring.gclog.ConditionalOnJcmd;
import com.nucleonforge.axelix.sbs.spring.gclog.DefaultGcLogService;
import com.nucleonforge.axelix.sbs.spring.gclog.GcLogEndpoint;
import com.nucleonforge.axelix.sbs.spring.gclog.JcmdExecutor;

/**
 * Auto-configuration for GC Log Endpoint functionality.
 *
 * <p>Configuration is conditional on the presence of JCMD utility and will not activate
 * if JCMD is not available in the system PATH.</p>
 *
 * @since 26.12.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
@ConditionalOnJcmd
public class GcLogEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JcmdExecutor jcmdExecutor() {
        return new JcmdExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGcLogService gcLogService(JcmdExecutor jcmdExecutor) {
        return new DefaultGcLogService(jcmdExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public GcLogEndpoint gcLogEndpoint(DefaultGcLogService gcLogService) {
        return new GcLogEndpoint(gcLogService);
    }
}
