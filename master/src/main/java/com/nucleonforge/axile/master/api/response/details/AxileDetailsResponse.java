package com.nucleonforge.axile.master.api.response.details;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.master.api.response.details.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.details.components.GitProfile;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
import com.nucleonforge.axile.master.api.response.details.components.RuntimeProfile;
import com.nucleonforge.axile.master.api.response.details.components.SpringProfile;

/**
 * The profile of a given details.
 *
 * @param serviceName  The name of the service providing information.
 * @param git          The profile of the git component response.
 * @param runtime      The profile of the runtime component response.
 * @param spring       The profile of the spring component response.
 * @param build        The profile of the build component response.
 * @param os           The profile of the OS component response.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AxileDetailsResponse(
        @Nullable String serviceName,
        @Nullable GitProfile git,
        @Nullable RuntimeProfile runtime,
        @Nullable SpringProfile spring,
        @Nullable BuildProfile build,
        @Nullable OSProfile os) {}
