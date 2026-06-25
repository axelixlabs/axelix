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

import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

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

    private static final String LOGBACK_FACTORY_FULL_CLASS_NAME = "ch.qos.logback.classic.LoggerContext";
    private static final String LOG_4_J_FACTORY_FULL_CLASS_NAME = "org.apache.logging.slf4j.Log4jLoggerFactory";

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

        ILoggerFactory activeFactory = LoggerFactory.getILoggerFactory();
        try {
            return switch (requiredSystem) {
                case LOGBACK -> Class.forName(LOGBACK_FACTORY_FULL_CLASS_NAME).isInstance(activeFactory);
                case LOG4J2 -> Class.forName(LOG_4_J_FACTORY_FULL_CLASS_NAME).isInstance(activeFactory);
            };
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
