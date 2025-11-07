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
