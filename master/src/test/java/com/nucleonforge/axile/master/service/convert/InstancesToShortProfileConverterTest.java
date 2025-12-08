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
package com.nucleonforge.axile.master.service.convert;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nucleonforge.axile.master.api.response.InstancesGridResponse.InstanceShortProfile;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.convert.response.InstancesToShortProfileConverter;

/**
 * Unit tests for {@link InstancesToShortProfileConverter}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InstancesToShortProfileConverter.class)
class InstancesToShortProfileConverterTest {

    @Autowired
    private InstancesToShortProfileConverter subject;

    @Test
    void shouldConvertInstanceToItsShortProfile() {
        // given.
        Instance input = Instancio.create(Instance.class);

        // when.
        InstanceShortProfile result = subject.convert(input);

        // then.
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("deployedFor", "instanceId")
                .isEqualTo(input);
    }
}
