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
package com.nucleonforge.axile.master.service.serde;

import org.springframework.stereotype.Component;

/**
 * {@link BinaryResourceMessageDeserializationStrategy} for logfile.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Component
public class LogFileMessageDeserializationStrategy extends BinaryResourceMessageDeserializationStrategy {

    @Override
    protected String filename() {
        return "application.log";
    }
}
