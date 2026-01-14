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

import com.nucleonforge.axelix.sbs.spring.context.ContextRestarter;
import com.nucleonforge.axelix.sbs.spring.context.DefaultContextRestarter;
import com.nucleonforge.axelix.sbs.spring.context.RestartListener;

/**
 * Auto-configuration for context restart support.
 *
 * <p>This configuration registers beans that handle application context restart events.
 * It provides a {@link ContextRestarter} bean responsible for triggering context restarts,
 * and a {@link RestartListener} bean that listens for restart events.</p>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class ContextRestarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextRestarter contextRestarter() {
        return new DefaultContextRestarter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestartListener restartListener() {
        return new RestartListener();
    }
}
