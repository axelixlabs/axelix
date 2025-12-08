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
package com.nucleonforge.axile.master.service.convert.response;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.master.api.response.ConfigPropsFeedResponse;
import com.nucleonforge.axile.master.api.response.ConfigPropsFeedResponse.ConfigPropsProfile;

/**
 * The {@link Converter} from {@link ConfigPropsFeed} to {@link ConfigPropsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ConfigPropsFeedConverter implements Converter<ConfigPropsFeed, ConfigPropsFeedResponse> {

    @Override
    public @NonNull ConfigPropsFeedResponse convertInternal(@NonNull ConfigPropsFeed source) {
        ConfigPropsFeedResponse response = new ConfigPropsFeedResponse();

        source.contexts().values().forEach(context -> {
            if (context != null && context.beans() != null) {
                context.beans()
                        .forEach((beanName, bean) -> response.addBean(
                                new ConfigPropsProfile(beanName, bean.prefix(), bean.properties(), bean.inputs())));
            }
        });

        return response;
    }
}
