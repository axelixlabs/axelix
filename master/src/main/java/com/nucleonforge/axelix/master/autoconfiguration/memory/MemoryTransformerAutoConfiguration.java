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
package com.nucleonforge.axelix.master.autoconfiguration.memory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.api.transform.BaseUnitParser;
import com.nucleonforge.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.nucleonforge.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;

/**
 * Auto-configuration for memory unit transformers.
 *
 * @since 18.12.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class MemoryTransformerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BaseUnitParser baseUnitParser() {
        return new BaseUnitParser();
    }

    @Bean
    public BytesMemoryBaseUnitValueTransformer bytesMemoryBaseUnitValueTransformer() {
        return new BytesMemoryBaseUnitValueTransformer();
    }

    @Bean
    public KilobytesMemoryBaseUnitValueTransformer kilobytesMemoryBaseUnitValueTransformer() {
        return new KilobytesMemoryBaseUnitValueTransformer();
    }
}
