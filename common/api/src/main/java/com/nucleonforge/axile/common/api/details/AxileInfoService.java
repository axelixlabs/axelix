package com.nucleonforge.axile.common.api.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.details.components.BuildInfo;
import com.nucleonforge.axile.common.api.details.components.CustomInfo;
import com.nucleonforge.axile.common.api.details.components.GitInfo;
import com.nucleonforge.axile.common.api.details.components.JavaInfo;
import com.nucleonforge.axile.common.api.details.components.OSInfo;
import com.nucleonforge.axile.common.api.details.components.ProcessInfo;
import com.nucleonforge.axile.common.api.details.components.SSLInfo;

/**
 * The response returned by the custom Axile info endpoint.
 *
 * @param git      The DTO containing git component information.
 * @param java     The DTO containing java component information.
 * @param process  The DTO containing process component information.
 * @param build    The DTO containing build component information.
 * @param os       The DTO containing OS component information
 * @param ssl      The DTO containing SSL component information
 * @param custom   The DTO containing custom component information
 *
 * @author Sergey Cherkasov
 */
public record AxileInfoService(
        @JsonProperty("git") @Nullable GitInfo git,
        @JsonProperty("java") @Nullable JavaInfo java,
        @JsonProperty("process") @Nullable ProcessInfo process,
        @JsonProperty("build") @Nullable BuildInfo build,
        @JsonProperty("os") @Nullable OSInfo os,
        @JsonProperty("ssl") @Nullable SSLInfo ssl,
        @JsonProperty("custom") @Nullable CustomInfo custom) {}
