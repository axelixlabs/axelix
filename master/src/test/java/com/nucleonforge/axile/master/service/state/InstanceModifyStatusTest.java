package com.nucleonforge.axile.master.service.state;

import java.util.UUID;

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
    private static final String activeInstanceId = UUID.randomUUID().toString();

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private InstanceModifyStatus modifyStatus;

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
        assertThatThrownBy(() -> modifyStatus.modifyStatus(activeInstanceId, Instance.InstanceStatus.RELOAD))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
