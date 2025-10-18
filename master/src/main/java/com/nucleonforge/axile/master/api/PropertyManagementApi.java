package com.nucleonforge.axile.master.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.api.request.PropertyUpdatedRequest;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.serde.MessageSerializationStrategy;
import com.nucleonforge.axile.master.service.transport.PropertyManagementEndpointProber;

/**
 * The API for managing properties.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.PropertyManagementApi.MAIN)
public class PropertyManagementApi {

    private final PropertyManagementEndpointProber propertyManagementEndpointProber;
    private final MessageSerializationStrategy messageSerializationStrategy;

    public PropertyManagementApi(
            PropertyManagementEndpointProber profileManagementEndpointProber,
            MessageSerializationStrategy messageSerializationStrategy) {
        this.propertyManagementEndpointProber = profileManagementEndpointProber;
        this.messageSerializationStrategy = messageSerializationStrategy;
    }

    @PostMapping(path = ApiPaths.PropertyManagementApi.INSTANCE_ID)
    public ResponseEntity<Void> changePropertyValue(
            @PathVariable("instanceId") String instanceId, @RequestBody PropertyUpdatedRequest request) {

        HttpPayload payload = HttpPayload.json(messageSerializationStrategy.serialize(request));
        propertyManagementEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
        return ResponseEntity.noContent().build();
    }
}
