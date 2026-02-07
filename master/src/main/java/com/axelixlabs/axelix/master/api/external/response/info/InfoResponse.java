/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.api.external.response.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.api.external.response.info.components.BuildProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.GitProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.JavaProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.OSProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.ProcessProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.SSLProfile;

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
