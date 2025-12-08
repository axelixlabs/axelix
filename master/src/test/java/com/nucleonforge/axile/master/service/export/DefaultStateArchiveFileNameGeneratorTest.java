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
package com.nucleonforge.axile.master.service.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.master.model.instance.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultStateArchiveFileNameGenerator}.
 *
 * @author Mikhail Polivakha
 */
class DefaultStateArchiveFileNameGeneratorTest {

    private DefaultStateArchiveFileNameGenerator subject;

    @BeforeEach
    void setUp() {
        subject = new DefaultStateArchiveFileNameGenerator();
    }

    @Test
    void shouldGenerateValidFileName() {
        // given.
        String instanceId = "ims-service-k02i302k-od20w";

        // when.
        String filename = subject.generate(InstanceId.of(instanceId));

        // then.
        assertThat(filename).contains(instanceId);
    }
}
