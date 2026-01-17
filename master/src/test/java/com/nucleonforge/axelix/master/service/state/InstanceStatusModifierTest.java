/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.nucleonforge.axelix.master.service.state;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;

import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
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
        registry.register(createInstance(instanceId, Instance.InstanceStatus.UP));

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
