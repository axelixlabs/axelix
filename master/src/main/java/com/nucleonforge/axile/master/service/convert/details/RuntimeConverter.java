package com.nucleonforge.axile.master.service.convert.details;

import java.util.Set;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.CustomInfo;
import com.nucleonforge.axile.common.api.details.components.JavaInfo;
import com.nucleonforge.axile.common.api.details.components.ProcessInfo;
import com.nucleonforge.axile.master.api.response.details.components.RuntimeProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

@Service
public class RuntimeConverter implements Converter<JavaInfo, RuntimeProfile> {
    @Autowired
    private CustomInfo customInfo;

    @Autowired
    private ProcessInfo processInfo;

    @Override
    public @NonNull RuntimeProfile convertInternal(@NonNull JavaInfo source) {
        return new RuntimeProfile(
                source.version(), customInfo.kotlinVersion(), source.vendor().name());
    }

    private String getGarbageCollector() {
        Set<ProcessInfo.Memory.GarbageCollectors> garbageCollectors =
                processInfo.memory().garbageCollectors();

        var name = garbageCollectors.stream().findFirst().get().name();
    }
}
