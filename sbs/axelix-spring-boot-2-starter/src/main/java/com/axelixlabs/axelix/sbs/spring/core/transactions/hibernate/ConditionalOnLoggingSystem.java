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

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional} annotation that restricts configuration components to a specific
 * underlying logging subsystem active in the current application context.
 *
 * <p>Usage example:
 * <pre>{@code
 * @Configuration
 * @ConditionalOnLoggingSystem(ConditionalOnLoggingSystem.System.LOG4J2)
 * public class MyLog4j2Configuration { ... }
 * }</pre>
 *
 * @author Vyacheslav Yanin
 * @see OnLoggingSystemCondition
 */
@Documented
@Conditional(OnLoggingSystemCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnLoggingSystem {

    System value();

    enum System {
        LOGBACK,
        LOG4J2
    }
}
