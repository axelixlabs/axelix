/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.serde.NoOpMessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * The {@link AbstractEndpointProber} that either do not return any value, or just discards the return value.
 *
 * @author Mikhail Polivakha
 */
public abstract class DiscardingAbstractEndpointProber extends AbstractEndpointProber<byte[]> {

    protected DiscardingAbstractEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry, NoOpMessageDeserializationStrategy.INSTANCE);
    }

    @Override
    public byte @NonNull [] invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException {
        return super.invoke(instanceId, httpPayload);
    }

    public void invokeNoValue(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException {
        super.invoke(instanceId, httpPayload);
    }
}
