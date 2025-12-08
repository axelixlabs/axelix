/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.export;

import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Abstraction that is capable to generate the file name for the state file archive.
 *
 * @author Mikhail Polivakha
 */
public interface StateArchiveFileNameGenerator {

    /**
     * Generation function.
     *
     * @param instanceId the id of the {@link Instance}, for which the filename is generated.
     * @return full file name (including extension) of the state archive for the {@link Instance} with the given {@link InstanceId}
     */
    String generate(InstanceId instanceId);
}
