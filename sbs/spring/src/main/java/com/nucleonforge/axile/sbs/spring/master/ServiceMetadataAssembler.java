package com.nucleonforge.axile.sbs.spring.master;

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;

/**
 * Assembles the metadata about this particular service
 *
 * @author Mikhail Polivakha
 */
public interface ServiceMetadataAssembler {

    /**
     * @return assembled {@link ServiceMetadata}.
     */
    ServiceMetadata assemble();
}
