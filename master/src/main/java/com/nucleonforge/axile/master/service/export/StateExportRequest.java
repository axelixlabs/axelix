package com.nucleonforge.axile.master.service.export;

import java.util.List;

import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.export.collect.StateComponent;

/**
 * Request for exporting the state of the given {@link Instance}.
 *
 * @author Mikhail Polivakha
 */
public record StateExportRequest(InstanceId instanceId, List<StateComponent> stateComponents) {}
