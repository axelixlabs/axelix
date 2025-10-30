package com.nucleonforge.axile.sbs.spring.details;

import com.nucleonforge.axile.common.api.AxileDetails;

/**
 * Assembles the details about this particular service.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
public interface ServiceDetailsAssembler {

    /**
     * @return assembled {@link AxileDetails}.
     */
    AxileDetails assemble();
}
