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
package com.nucleonforge.axile.master.service.convert.response.info.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.master.api.response.info.components.BuildProfile;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link BuildInfo} to {@link BuildProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class BuildInfoConverter implements Converter<BuildInfo, BuildProfile> {

    @Override
    public @NonNull BuildProfile convertInternal(@NonNull BuildInfo source) {
        return new BuildProfile(source.artifact(), source.name(), source.version(), source.group(), source.time());
    }
}
