package com.nucleonforge.axile.master.api;

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.ThreadDumpEndpointProber;

/**
 * The API for thread dump.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.ThreadDumpApi.MAIN)
public class ThreadDumpApi {

    private final ThreadDumpEndpointProber threadDumpEndpointProber;
    private final Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter;

    public ThreadDumpApi(
            ThreadDumpEndpointProber threadDumpEndpointProber,
            Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter) {
        this.threadDumpEndpointProber = threadDumpEndpointProber;
        this.converter = converter;
    }

    @GetMapping(ApiPaths.ThreadDumpApi.INSTANCE_ID)
    public ThreadDumpFeedResponse getThreadDump(@PathVariable("instanceId") String instanceId) {
        ThreadDumpFeed result = threadDumpEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(converter.convert(result));
    }
}
