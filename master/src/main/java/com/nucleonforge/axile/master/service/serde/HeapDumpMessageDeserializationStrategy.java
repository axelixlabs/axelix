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
 * {@link BinaryResourceMessageDeserializationStrategy} for heapdump.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Component
public class HeapDumpMessageDeserializationStrategy extends BinaryResourceMessageDeserializationStrategy {

    /**
     * This filename extension is valid only for HotSpot heapdump format.
     * <p>
     * Although, in 99% of cases hprof is the format of the actual binary
     * deserialized file, it might still be that someone is using OpenJ9 for
     * instance or anything, and head dump format would differ from hprof.
     */
    @Override
    protected String filename() {
        return "heapdump.hprof";
    }
}
