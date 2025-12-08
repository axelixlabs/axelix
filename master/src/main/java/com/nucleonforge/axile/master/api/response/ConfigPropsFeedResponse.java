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
package com.nucleonforge.axile.master.api.response;

import java.util.ArrayList;
import java.util.List;

import com.nucleonforge.axile.common.api.KeyValue;

/**
 * The feed of {@code @ConfigurationProperties} beans used in the application.
 *
 * @param beans  The unified list of beans that contains beans from one or more contexts.
 *
 * @author Sergey Cherkasov
 */
public record ConfigPropsFeedResponse(List<ConfigPropsProfile> beans) {

    public ConfigPropsFeedResponse() {
        this(new ArrayList<>());
    }

    public ConfigPropsFeedResponse addBean(ConfigPropsProfile beanProfile) {
        this.beans.add(beanProfile);
        return this;
    }

    /**
     * The profile of a given {@code @ConfigurationProperties} bean.
     *
     * @param beanName     The name of the bean.
     * @param prefix       The prefix applied to the names of the bean properties.
     * @param properties   The properties of the bean as name-value pairs.
     * @param inputs       The origin and value of each configuration parameter
     *                     — which value was applied and from which source
     *                     — to configure a specific property.
     *
     * @author Sergey Cherkasov
     */
    public record ConfigPropsProfile(
            String beanName, String prefix, List<KeyValue> properties, List<KeyValue> inputs) {}
}
