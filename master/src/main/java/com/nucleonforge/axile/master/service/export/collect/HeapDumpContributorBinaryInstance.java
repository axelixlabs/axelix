package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.HeapDumpApi;
import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.HeapDumpStateComponentSettings;

/**
 * Collect Heap Dump information for application state export.
 *
 * @see HeapDumpApi
 * @since 20.11.2025
 * @author Nikita Kirillov
 */
@Component
public class HeapDumpContributorBinaryInstance
        extends AbstractBinaryInstanceStateCollector<HeapDumpStateComponentSettings> {

    private final HeapDumpApi heapDumpApi;

    public HeapDumpContributorBinaryInstance(HeapDumpApi heapDumpApi) {
        this.heapDumpApi = heapDumpApi;
    }

    @Override
    protected Resource collectBinaryResource(String instanceId, HeapDumpStateComponentSettings settings)
            throws StateExportException {
        ResponseEntity<Resource> heapDump = heapDumpApi.getHeapDump(instanceId, settings.sanitized());
        if (heapDump.getBody() == null) {
            throw new StateExportException(
                    instanceId, "Heap dump endpoint returned successful status but empty content");
        }
        return heapDump.getBody();
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.HEAP_DUMP;
    }
}
