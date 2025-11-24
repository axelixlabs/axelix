package com.nucleonforge.axile.master.service.convert.response.info;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.ServiceInfo;
import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.common.api.info.components.GitInfo;
import com.nucleonforge.axile.common.api.info.components.JavaInfo;
import com.nucleonforge.axile.common.api.info.components.OSInfo;
import com.nucleonforge.axile.common.api.info.components.ProcessInfo;
import com.nucleonforge.axile.common.api.info.components.SSLInfo;
import com.nucleonforge.axile.master.api.response.info.InfoResponse;
import com.nucleonforge.axile.master.api.response.info.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.info.components.GitProfile;
import com.nucleonforge.axile.master.api.response.info.components.JavaProfile;
import com.nucleonforge.axile.master.api.response.info.components.OSProfile;
import com.nucleonforge.axile.master.api.response.info.components.ProcessProfile;
import com.nucleonforge.axile.master.api.response.info.components.SSLProfile;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.convert.response.info.components.BuildInfoConverter;
import com.nucleonforge.axile.master.service.convert.response.info.components.GitInfoConverter;
import com.nucleonforge.axile.master.service.convert.response.info.components.JavaInfoConverter;
import com.nucleonforge.axile.master.service.convert.response.info.components.OSInfoConverter;
import com.nucleonforge.axile.master.service.convert.response.info.components.ProcessInfoConverter;
import com.nucleonforge.axile.master.service.convert.response.info.components.SSLInfoConverter;

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
