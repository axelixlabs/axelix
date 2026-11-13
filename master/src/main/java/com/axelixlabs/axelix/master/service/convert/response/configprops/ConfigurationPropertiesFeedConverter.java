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
package com.axelixlabs.axelix.master.service.convert.response.configprops;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.utils.PropertyNameNormalizer;
import com.axelixlabs.axelix.master.api.external.response.configprops.ConfigurationPropertiesFeedResponse;
import com.axelixlabs.axelix.master.api.external.response.configprops.ConfigurationPropertiesFeedResponse.DangerousValue;
import com.axelixlabs.axelix.master.api.external.response.configprops.ConfigurationPropertiesFeedResponse.Entry;
import com.axelixlabs.axelix.master.domain.DangerousPropertyValue;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.transport.PropertySanitizer;

/**
 * The {@link Converter} from {@link ConfigurationPropertiesFeed} to {@link ConfigurationPropertiesFeedResponse}.
 *
 * @author Nikita Kirillov
 */
@Service
public class ConfigurationPropertiesFeedConverter
        implements Converter<ConfigurationPropertiesFeed, ConfigurationPropertiesFeedResponse> {

    private final PropertySanitizer propertySanitizer;
    private final PropertyNameNormalizer propertyNameNormalizer;

    public ConfigurationPropertiesFeedConverter(
            PropertySanitizer propertySanitizer, PropertyNameNormalizer propertyNameNormalizer) {
        this.propertySanitizer = propertySanitizer;
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    public @NonNull ConfigurationPropertiesFeedResponse convertNonNull(@NonNull ConfigurationPropertiesFeed source) {
        propertySanitizer.sanitize(source);
        return new ConfigurationPropertiesFeedResponse(source.getBeans().stream()
                .map(bean -> new ConfigurationPropertiesFeedResponse.Bean(
                        bean.getBeanName(),
                        bean.getPrefix(),
                        convertEntries(bean.getProperties(), bean.getPrefix()),
                        convertEntries(bean.getInputs(), null)))
                .toList());
    }

    private List<Entry> convertEntries(List<ConfigurationPropertiesEntry> entries, @Nullable String prefix) {
        return entries.stream()
                .map(entry -> new Entry(
                        entry.getKey(), entry.getValue(), prefix == null ? null : detectDangerousValue(prefix, entry)))
                .toList();
    }

    private @Nullable DangerousValue detectDangerousValue(String prefix, ConfigurationPropertiesEntry entry) {
        String entryKey = entry.getKey();
        String name = prefix.isEmpty() ? entryKey : prefix + "." + entryKey;
        // Match collection elements against the dictionary entry without the index, e.g. include[0] -> include.
        String normalizedName = propertyNameNormalizer.normalize(name.replaceAll("\\[\\d+\\]", ""));
        DangerousPropertyValue.DangerousValue dangerousValue =
                DangerousPropertyValue.resolve(normalizedName, entry.getValue());
        return dangerousValue == null
                ? null
                : new DangerousValue(dangerousValue.rationale(), dangerousValue.alternative());
    }
}
