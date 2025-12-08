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
package com.nucleonforge.axile.master.service.export.collect;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * Collector for application state data export functionality.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public interface InstanceStateCollector<T extends StateComponentSettings> {

    /**
     * @return the {@link StateComponent state export component} that this collector is responsible for.
     */
    StateComponent responsibleFor();

    /**
     * Collects data from the specified application instance.
     *
     * @param instanceId the identifier of the application instance to collect data from
     * @return the collected data as the byte array
     */
    byte[] collect(String instanceId, T settings) throws StateExportException;
}
