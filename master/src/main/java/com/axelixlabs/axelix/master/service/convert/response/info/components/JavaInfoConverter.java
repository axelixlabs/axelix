/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.convert.response.info.components;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.info.components.JavaInfo;
import com.axelixlabs.axelix.master.api.external.response.info.components.JavaProfile;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

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
