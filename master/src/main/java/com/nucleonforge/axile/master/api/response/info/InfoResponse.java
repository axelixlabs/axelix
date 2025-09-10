package com.nucleonforge.axile.master.api.response.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.master.api.response.info.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.info.components.GitProfile;
import com.nucleonforge.axile.master.api.response.info.components.JavaProfile;
import com.nucleonforge.axile.master.api.response.info.components.OSProfile;
import com.nucleonforge.axile.master.api.response.info.components.ProcessProfile;
import com.nucleonforge.axile.master.api.response.info.components.SSLProfile;

/**
 * The profile of a given info.
 *
 * @param git     The short profile of the git component response, if available.
 * @param build   The short profile of the build component response, if available.
 * @param os      The short profile of the OS component response, if available.
 * @param process The short profile of the process component response, if available.
 * @param java    The short profile of the java component response, if available.
 * @param ssl     The short profile of the SSL component response, if available (present since version 3.4.9).
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InfoResponse(
        @Nullable GitProfile git,
        @Nullable BuildProfile build,
        @Nullable OSProfile os,
        @Nullable ProcessProfile process,
        @Nullable JavaProfile java,
        @Nullable SSLProfile ssl) {}
