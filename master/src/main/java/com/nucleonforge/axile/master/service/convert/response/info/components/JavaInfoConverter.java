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
package com.nucleonforge.axile.master.service.convert.response.info.components;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.components.JavaInfo;
import com.nucleonforge.axile.master.api.response.info.components.JavaProfile;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link JavaInfo} to {@link JavaProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class JavaInfoConverter implements Converter<JavaInfo, JavaProfile> {

    @Override
    public @NonNull JavaProfile convertInternal(@NonNull JavaInfo source) {
        JavaProfile.Vendor vendor = convertVendor(source);
        JavaProfile.Runtime runtime = convertRuntime(source);
        JavaProfile.JVM jvm = convertJvm(source);

        return new JavaProfile(source.version(), vendor, runtime, jvm);
    }

    private JavaProfile.@Nullable JVM convertJvm(JavaInfo source) {
        JavaInfo.JVM jvm = source.jvm();
        if (jvm != null) {
            return new JavaProfile.JVM(jvm.name(), jvm.vendor(), jvm.version());
        }

        return null;
    }

    private JavaProfile.@Nullable Runtime convertRuntime(JavaInfo source) {
        JavaInfo.Runtime runtime = source.runtime();
        if (runtime != null) {
            return new JavaProfile.Runtime(runtime.name(), runtime.version());
        }

        return null;
    }

    private JavaProfile.@Nullable Vendor convertVendor(JavaInfo source) {
        JavaInfo.Vendor vendor = source.vendor();
        if (vendor != null) {
            return new JavaProfile.Vendor(vendor.name(), vendor.version());
        }

        return null;
    }
}
