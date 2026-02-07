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
package com.axelixlabs.axelix.master.service.convert.response.info;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.info.ServiceInfo;
import com.axelixlabs.axelix.common.api.info.components.BuildInfo;
import com.axelixlabs.axelix.common.api.info.components.GitInfo;
import com.axelixlabs.axelix.common.api.info.components.JavaInfo;
import com.axelixlabs.axelix.common.api.info.components.OSInfo;
import com.axelixlabs.axelix.common.api.info.components.ProcessInfo;
import com.axelixlabs.axelix.common.api.info.components.SSLInfo;
import com.axelixlabs.axelix.master.api.external.response.info.InfoResponse;
import com.axelixlabs.axelix.master.api.external.response.info.components.BuildProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.GitProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.JavaProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.OSProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.ProcessProfile;
import com.axelixlabs.axelix.master.api.external.response.info.components.SSLProfile;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.convert.response.info.components.BuildInfoConverter;
import com.axelixlabs.axelix.master.service.convert.response.info.components.GitInfoConverter;
import com.axelixlabs.axelix.master.service.convert.response.info.components.JavaInfoConverter;
import com.axelixlabs.axelix.master.service.convert.response.info.components.OSInfoConverter;
import com.axelixlabs.axelix.master.service.convert.response.info.components.ProcessInfoConverter;
import com.axelixlabs.axelix.master.service.convert.response.info.components.SSLInfoConverter;

/**
 * The {@link Converter} from {@link ServiceInfo} to {@link InfoResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ServiceInfoConverter implements Converter<ServiceInfo, InfoResponse> {
    private final BuildInfoConverter buildInfoConverter;
    private final GitInfoConverter gitInfoConverter;
    private final JavaInfoConverter javaInfoConverter;
    private final OSInfoConverter osInfoConverter;
    private final SSLInfoConverter sslInfoConverter;
    private final ProcessInfoConverter processInfoConverter;

    public ServiceInfoConverter(
            BuildInfoConverter buildInfoConverter,
            GitInfoConverter gitInfoConverter,
            JavaInfoConverter javaInfoConverter,
            OSInfoConverter osInfoConverter,
            SSLInfoConverter sslInfoConverter,
            ProcessInfoConverter processInfoConverter) {
        this.buildInfoConverter = buildInfoConverter;
        this.gitInfoConverter = gitInfoConverter;
        this.javaInfoConverter = javaInfoConverter;
        this.osInfoConverter = osInfoConverter;
        this.sslInfoConverter = sslInfoConverter;
        this.processInfoConverter = processInfoConverter;
    }

    @Override
    public @NonNull InfoResponse convertInternal(@NonNull ServiceInfo source) {

        BuildInfo buildInfo = source.build();
        GitInfo gitInfo = source.git();
        JavaInfo javaInfo = source.java();
        OSInfo osInfo = source.os();
        SSLInfo sslInfo = source.ssl();
        ProcessInfo processInfo = source.process();

        BuildProfile buildProfile = buildInfoConverter.convert(buildInfo);
        GitProfile gitProfile = gitInfoConverter.convert(gitInfo);
        JavaProfile javaProfile = javaInfoConverter.convert(javaInfo);
        OSProfile osProfile = osInfoConverter.convert(osInfo);
        SSLProfile sslProfile = sslInfoConverter.convert(sslInfo);
        ProcessProfile processProfile = processInfoConverter.convert(processInfo);

        InfoResponse infoResponse =
                new InfoResponse(gitProfile, buildProfile, osProfile, processProfile, javaProfile, sslProfile);

        return infoResponse;
    }
}
