package com.nucleonforge.axile.master.service.export;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.export.collect.InstanceStateCollector;

/**
 * Service for exporting the state of the given {@link Instance}.
 * <p>
 * The "state" of the given instance is assembled by {@link InstanceStateCollector JsonInstanceStateCollectors}.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
public interface InstanceStateExporter {

    /**
     * Exports state of the specified application instance.
     *
     * @param request request that accumulates all the info required for state export.
     * @return byte array containing the exported state data.
     * @throws StateExportException if export process fails.
     */
    byte[] exportInstanceState(StateExport request, InstanceId instanceId) throws StateExportException;
}
