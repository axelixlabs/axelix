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
package com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate;

import org.slf4j.ILoggerFactory;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * {@link Condition} implementation that checks if the active SLF4J {@link ILoggerFactory}
 * matches the required logging system specified by {@link ConditionalOnLoggingSystem}.
 *
 * <p>This condition performs dynamic, type-safe runtime validation and safely prevents {@link NoClassDefFoundError}
 * when a specific logging implementation is missing from the classpath.
 *
 * @author Vyacheslav Yanin
 */
public class OnLoggingSystemCondition implements Condition {

    private static final String LOGBACK_LOGGING_SYSTEM_CLASS_NAME =
        "org.springframework.boot.logging.logback.LogbackLoggingSystem";
    private static final String LOG4J2_LOGGING_SYSTEM_FULL_CLASS_NAME =
        "org.springframework.boot.logging.log4j2.Log4J2LoggingSystem";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnLoggingSystem.class.getName());
        if (attributes == null) {
            return false;
        }

        ConditionalOnLoggingSystem.System requiredSystem = (ConditionalOnLoggingSystem.System) attributes.get("value");
        if (requiredSystem == null) {
            return false;
        }

        LoggingSystem loggingSystem = LoggingSystem.get(getClass().getClassLoader());
        switch (requiredSystem) {
            case LOGBACK:
                return isInstance(LOGBACK_LOGGING_SYSTEM_CLASS_NAME, loggingSystem);
            case LOG4J2:
                return isInstance(LOG4J2_LOGGING_SYSTEM_FULL_CLASS_NAME, loggingSystem);
            default:
                return false;
        }
    }

    private static boolean isInstance(String loggingSystemClassName, LoggingSystem loggingSystem) {
        try {
            return Class.forName(loggingSystemClassName).isInstance(loggingSystem);
        } catch (ClassNotFoundException | LinkageError e) {
            // if we were not able to load the LoggingSystem class, then LoggingSystem used by Spring Boot is definitely
            // not the one that we're checking.
            return false;
        }
    }
}
