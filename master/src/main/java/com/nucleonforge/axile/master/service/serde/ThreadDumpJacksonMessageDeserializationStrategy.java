/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ThreadDumpFeed}.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Component
public class ThreadDumpJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ThreadDumpFeed> {

    public ThreadDumpJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ThreadDumpFeed> supported() {
        return ThreadDumpFeed.class;
    }
}
