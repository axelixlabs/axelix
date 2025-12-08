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

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The exception that occurs when Axile Master tried to reach a particular {@link ActuatorEndpoint}
 * on the managed service, but the managed service is either not available, or responded with non 2xx status
 * code.
 *
 * @author Mikhail Polivakha
 */
public class EndpointInvocationException extends RuntimeException {

    public EndpointInvocationException(Throwable cause) {
        super(cause);
    }

    public EndpointInvocationException(String message) {
        super(message);
    }
}
