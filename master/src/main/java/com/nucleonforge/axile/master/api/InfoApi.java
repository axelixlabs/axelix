package com.nucleonforge.axile.master.api;

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.info.ServiceInfo;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.info.InfoResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.InfoEndpointProber;

/**
 * The API for managing info.
 *
 * @author Sergey Cherkasov
 */
@RestController
@RequestMapping(path = ApiPaths.InfoApi.MAIN)
public class InfoApi {

    private final InfoEndpointProber infoEndpointProber;
    private final Converter<ServiceInfo, InfoResponse> converter;

    public InfoApi(InfoEndpointProber infoEndpointProber, Converter<ServiceInfo, InfoResponse> converter) {
        this.infoEndpointProber = infoEndpointProber;
        this.converter = converter;
    }

    @GetMapping(path = ApiPaths.InfoApi.INSTANCE_ID)
    public InfoResponse getInfoResponse(@PathVariable("instanceId") String instanceId) {
        ServiceInfo serviceInfo = infoEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(serviceInfo));
    }
}
