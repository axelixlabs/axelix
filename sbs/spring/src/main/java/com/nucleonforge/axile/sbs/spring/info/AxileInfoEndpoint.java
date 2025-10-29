package com.nucleonforge.axile.sbs.spring.info;

import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "axile-info")
public class AxileInfoEndpoint {

    private final ServiceInfoAssembler serviceInfoAssembler;

    public AxileInfoEndpoint(ServiceInfoAssembler serviceInfoAssembler) {
        this.serviceInfoAssembler = serviceInfoAssembler;
    }

    @ReadOperation
    public Map<String, Object> info() {
        return serviceInfoAssembler.assemble();
    }
}
