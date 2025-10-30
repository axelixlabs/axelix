package com.nucleonforge.axile.common.api.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.details.components.BuildDetails;
import com.nucleonforge.axile.common.api.details.components.GitDetails;
import com.nucleonforge.axile.common.api.details.components.OsDetails;
import com.nucleonforge.axile.common.api.details.components.RuntimeDetails;
import com.nucleonforge.axile.common.api.details.components.SpringDetails;

/**
 * The response returned by the custom Axile details endpoint.
 *
 * @param git      The DTO containing git component details.
 * @param spring   The DTO containing spring component details.
 * @param runtime  The DTO containing runtime component details.
 * @param build    The DTO containing build component details.
 * @param os       The DTO containing OS component details.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public record AxileDetails(
        @Nullable String instanceName,
        @JsonProperty("git") @Nullable GitDetails git,
        @JsonProperty("spring") SpringDetails spring,
        @JsonProperty("runtime") RuntimeDetails runtime,
        @JsonProperty("build") @Nullable BuildDetails build,
        @JsonProperty("os") OsDetails os) {

    public AxileDetails setInstanceName(String instanceName) {
        return new AxileDetails(
            instanceName,
            this.git,
            this.spring,
            this.runtime,
            this.build,
            this.os
        );
    }
}
