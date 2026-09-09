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

import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.master.domain.database.OssRdbms;

/**
 * Condition to activate certain parts of configuration only in case the given {@link OssRdbms} is active.
 *
 * @author Mikhail Polivakha
 */
public class OnCommunityRdbmsCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MergedAnnotation<ConditionalOnCommunityRdbms> annotation =
                metadata.getAnnotations().get(ConditionalOnCommunityRdbms.class);

        OssRdbms expected = annotation.getEnum("value", OssRdbms.class);

        String driverClassName = expected.driverClassName();

        if (ClassUtils.isPresent(driverClassName, getClass().getClassLoader())) {
            String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");

            Assert.notNull(jdbcUrl, "Axelix Master requires the database to work with, so JDBC url must be configured");

            OssRdbms actual = OssRdbms.fromJdbcUrl(jdbcUrl);

            if (Objects.equals(actual, expected)) {
                return ConditionOutcome.match();
            } else {
                return ConditionOutcome.noMatch(
                        "Expected to work with '%s' database, but actually '%s' database is in use"
                                .formatted(expected.name(), actual.name()));
            }

        } else {
            return ConditionOutcome.noMatch("For the '%s' database unable to find driver class '%s' in classpath"
                    .formatted(expected.name(), driverClassName));
        }
    }
}
