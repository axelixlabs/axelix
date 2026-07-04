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
package com.axelixlabs.axelix.master.autoconfiguration.database;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.master.domain.database.CommunityRDBMS;

/**
 * Condition to activate certain parts of configuration only in case the given {@link CommunityRDBMS} is active.
 *
 * @author Mikhail Polivakha
 */
public class OnCommunityRdbmsCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MergedAnnotation<ConditionalOnCommunityRdbms> annotation =
                metadata.getAnnotations().get(ConditionalOnCommunityRdbms.class);

        CommunityRDBMS database = annotation.getEnum("value", CommunityRDBMS.class);

        String driverClassName = database.driverClassName();

        if (ClassUtils.isPresent(driverClassName, getClass().getClassLoader())) {
            return checkJdbcUrlPrefix(context, database.jdbcUrlPrefix());
        } else {
            return ConditionOutcome.noMatch("For the '%s' database unable to find driver class '%s' in classpath"
                    .formatted(database.name(), driverClassName));
        }
    }

    private static @NonNull ConditionOutcome checkJdbcUrlPrefix(
            ConditionContext context, String expectedJdbcUrlPrefix) {
        String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");

        if (jdbcUrl != null && jdbcUrl.startsWith(expectedJdbcUrlPrefix)) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("jdbcUrlPrefix was expected to be '%s', but JDBC url actually is '%s'"
                    .formatted(expectedJdbcUrlPrefix, jdbcUrl));
        }
    }
}
