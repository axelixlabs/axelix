package com.nucleonforge.axile.sbs.spring.metrics;

import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;

/**
 * Assembles the metrics groups about this particular service.
 *
 * @author Sergey Cherkasov
 */
public interface ServiceMetricsGroupsAssembler {

    /**
     * @return assembled {@link AxileMetricsGroups}.
     */
    AxileMetricsGroups assemble();
}
