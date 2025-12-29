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
package com.nucleonforge.axelix.master.service;

import com.nucleonforge.axelix.master.api.response.DashboardResponse;

/**
 * Service that collects information necessary for dashboard rendering.
 *
 * @author Mikhail Polivakha
 */
public interface DashboardService {

    /**
     * It is intentional that here we return the DTO from the API layer inside the service layer.
     * As of time of writing this code, there is not that much reasoning to split the model returned
     * by the service layer from the {@link DashboardResponse} DTO returned from the UI layer.
     * @return the {@link DashboardResponse}.
     */
    DashboardResponse getDashboardInfo();
}
