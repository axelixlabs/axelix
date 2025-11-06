package com.nucleonforge.axile.master.service.state;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InstanceModifyStatus}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class InstanceModifyStatusTest {
    @Autowired
    private InstanceRegistry registry;

    private final String activeInstanceId = UUID.randomUUID().toString();

    private InstanceModifyStatus modifyStatus;

    @BeforeEach
    void prepare() {
        modifyStatus = new InstanceModifyStatus(registry);
    }

    @Test
    void shouldInstanceModifyStatus() {
        registry.register(createInstanceWithStatus(activeInstanceId, Instance.InstanceStatus.UP));

        Instance instance = registry.get(InstanceId.of(activeInstanceId)).orElseThrow(InstanceNotFoundException::new);
        assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.UP);

        modifyStatus.modifyStatus(activeInstanceId, Instance.InstanceStatus.RELOAD);

        Instance instanceModify =
                registry.get(InstanceId.of(activeInstanceId)).orElseThrow(InstanceNotFoundException::new);
        assertThat(instanceModify.status()).isEqualTo(Instance.InstanceStatus.RELOAD);
    }

    @Test
    void shouldInstanceNotFoundException() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> modifyStatus.modifyStatus(instanceId, Instance.InstanceStatus.RELOAD))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
