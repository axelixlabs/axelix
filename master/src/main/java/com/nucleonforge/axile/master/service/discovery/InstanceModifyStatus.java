package com.nucleonforge.axile.master.service.discovery;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * Service for changing the status of an {@link Instance}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class InstanceModifyStatus {

    private final InstanceRegistry instanceRegistry;

    public InstanceModifyStatus(InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
    }

    public void modifyStatus(String instanceId, Instance.InstanceStatus instanceStatus) {
        Instance instance = instanceRegistry.get(InstanceId.of(instanceId)).orElseThrow(InstanceNotFoundException::new);
        Instance instanceNew = instance.copy(instanceStatus);
        instanceRegistry.refresh(instanceNew);
    }
}
