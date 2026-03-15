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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionType;

/**
 * Default implementation {@link ValueAnnotationInjectionProcessor}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultValueAnnotationInjectionProcessor implements ValueAnnotationInjectionProcessor {
    // Looks up for the pattern @Value("${my.property:123}"), works also if the default value is absent.
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+?)(?::(.+?))?}");

    // Looks up for the pattern ".getProperty()", such as "#{environment.getProperty('server.port')}"
    // Note that as of now we're not checking that the actual receiver is instanceof Spring's Environment
    private static final Pattern ENVIRONMENT_GET_PROPERTY_CALLS_PATTERN =
            Pattern.compile("getProperty\\s*\\(\\s*['\"]([^'\"]+)['\"]");

    // Looks up for the pattern that uses property source name indexed usage, such as
    // "#{systemProperties['server.port']}"
    private final Pattern SYSTEM_PROPERTIES_ACCESS_PATTERN = Pattern.compile("\\[\\s*['\"]([^'\"]+)['\"]\\s*]");

    private final PropertyNameNormalizer propertyNameNormalizer;

    public DefaultValueAnnotationInjectionProcessor(PropertyNameNormalizer propertyNameNormalizer) {
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    public void processValueAnnotation(
            Map<String, List<InjectionPoint>> propertyToInjectionPoints,
            String expression,
            String beanName,
            InjectionType injectionType,
            String targetName) {

        List<String> propertyNames = extractPropertyNamesFromExpression(expression);

        for (String propertyName : propertyNames) {
            String normalizedName = propertyNameNormalizer.normalize(propertyName);

            InjectionPoint injectionPoint = new InjectionPoint(beanName, injectionType, targetName, expression);

            propertyToInjectionPoints
                    .computeIfAbsent(normalizedName, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(injectionPoint);
        }
    }

    private List<String> extractPropertyNamesFromExpression(String expression) {
        List<String> propertyNames = new ArrayList<>();

        // ${property.name}
        Matcher matcher = PROPERTY_PATTERN.matcher(expression);
        while (matcher.find()) {
            String propertyExpression = matcher.group(1).trim();
            propertyNames.add(propertyExpression);
        }

        // SpEL
        if (expression.contains("#{")) {
            extractPropertyNamesFromSpEL(expression, propertyNames);
        }

        return propertyNames;
    }

    private void extractPropertyNamesFromSpEL(String expression, List<String> propertyNames) {
        Matcher matcher = ENVIRONMENT_GET_PROPERTY_CALLS_PATTERN.matcher(expression);
        while (matcher.find()) {
            propertyNames.add(matcher.group(1));
        }

        matcher = SYSTEM_PROPERTIES_ACCESS_PATTERN.matcher(expression);
        while (matcher.find()) {
            propertyNames.add(matcher.group(1));
        }
    }
}
