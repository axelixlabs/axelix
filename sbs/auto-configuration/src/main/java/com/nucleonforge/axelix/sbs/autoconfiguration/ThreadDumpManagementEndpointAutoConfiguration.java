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

import com.nucleonforge.axelix.sbs.spring.threaddump.DefaultThreadDumpContentionMonitoringManagement;
import com.nucleonforge.axelix.sbs.spring.threaddump.ThreadDumpContentionMonitoringManagement;
import com.nucleonforge.axelix.sbs.spring.threaddump.ThreadDumpManagementEndpoint;

/**
 * Auto-configuration for Thread Dump management functionality.
 *
 * @author Sergey Cherkasov
 */
@AutoConfiguration
public class ThreadDumpManagementEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ThreadDumpContentionMonitoringManagement management() {
        return new DefaultThreadDumpContentionMonitoringManagement();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadDumpManagementEndpoint threadDumpManagementEndpoint(
            ThreadDumpContentionMonitoringManagement management) {
        return new ThreadDumpManagementEndpoint(management);
    }
}
