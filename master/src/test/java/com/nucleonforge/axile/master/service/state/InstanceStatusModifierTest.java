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
package com.nucleonforge.axile.master.service.state;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InstanceStatusModifier}.
 *
 * @author Sergey Cherkasov
 */
public class InstanceStatusModifierTest {

    private final InstanceRegistry registry = new InMemoryInstanceRegistry();
    private final InstanceStatusModifier modifyStatus = new InstanceStatusModifier(registry);

    @Test
    void shouldInstanceModifyStatus() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstanceWithStatus(instanceId, Instance.InstanceStatus.UP));

        // when.
        modifyStatus.modifyStatus(instanceId, Instance.InstanceStatus.RELOAD);

        // then.
        Instance instanceModify = registry.get(InstanceId.of(instanceId)).orElseThrow(InstanceNotFoundException::new);
        assertThat(instanceModify.status()).isEqualTo(Instance.InstanceStatus.RELOAD);
    }

    @Test
    void shouldInstanceNotFoundException() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        assertThatThrownBy(() -> modifyStatus.modifyStatus(instanceId, Instance.InstanceStatus.RELOAD))
                // then.
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
