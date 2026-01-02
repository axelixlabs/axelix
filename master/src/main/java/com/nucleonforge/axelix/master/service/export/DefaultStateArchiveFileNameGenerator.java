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
package com.nucleonforge.axelix.master.service.export;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.nucleonforge.axelix.master.model.instance.InstanceId;

/**
 * Default implementation of {@link StateArchiveFileNameGenerator}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultStateArchiveFileNameGenerator implements StateArchiveFileNameGenerator {

    public static final String STATE_ARCHIVE_FILE_TEMPLATE = "instance-state-%s-%s.zip";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'mm-HH-ss");

    @Override
    public String generate(InstanceId instanceId) {
        return STATE_ARCHIVE_FILE_TEMPLATE.formatted(
                instanceId.instanceId(), FORMATTER.format(Instant.now().atZone(ZoneOffset.systemDefault())));
    }
}
