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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Default implementation {@link PropertySourceDescriptionResolver}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultPropertySourceDescriptionResolver implements PropertySourceDescriptionResolver {

    /**
     * Matches Spring's config resource property source names to extract the file name and location.
     * <p>
     * Example: "Config resource 'class path resource [application-dev.properties]' via location 'optional:classpath:/'"
     * Example: "Config resource 'file [/etc/app/application-prod.yaml]' via location 'optional:file:/etc/app/'"
     * <p>
     * Well, yes, this approach is not that reliable, and we know that. However, the problem is that the name of the given
     * {org.springframework.core.env.PropertySource}, especially those that we care about, i.e.
     * <ol>
     *     <li>{org.springframework.boot.env.OriginTrackedMapPropertySource})</li>
     *     <li>{org.springframework.core.io.support.ResourcePropertySource})</li>
     * </ol>
     *
     * contains all the information required for us to present it in a user-friendly way. And, apparently, it is the easiest
     * approach out there to get this info, since all the PropertySources above loose all the information about the underlying
     * {org.springframework.core.io.Resource}, it is just gone at runtime. We can of course try to overcome this by writing
     * custom machinery around PropertySources, but that just does not justify the engineering effort.
     */
    private static final Pattern CONFIG_RESOURCE_PATTERN =
            Pattern.compile("Config resource '(?:class path resource|file) \\[([^]]+)]' via location '([^']+)'");

    public PropertySourceDisplayData resolveDisplayData(String sourceName, PropertySourceDescription[] descriptions) {
        if (sourceName.startsWith("Config resource")) {
            Matcher matcher = CONFIG_RESOURCE_PATTERN.matcher(sourceName);
            if (matcher.find()) {
                String displayName = Paths.get(matcher.group(1)).getFileName().toString();
                String description = String.format(
                        "Properties that are loaded from %s located in %s", displayName, matcher.group(2));
                return new PropertySourceDisplayData(displayName, description);
            }
        }

        return new PropertySourceDisplayData(sourceName, getDescriptionBySourceName(sourceName, descriptions));
    }

    private static @Nullable String getDescriptionBySourceName(
            String sourceName, PropertySourceDescription[] descriptions) {
        return findBySourceName(sourceName, descriptions)
                .map(PropertySourceDescription::getDescription)
                .orElse(null);
    }

    private static <T extends PropertySourceDescription> Optional<T> findBySourceName(
            String sourceName, T[] descriptions) {
        return Arrays.stream(descriptions)
                .filter(desc -> desc.getSourceName().equals(sourceName) || sourceName.startsWith(desc.getSourceName()))
                .findFirst();
    }
}
