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
package com.axelixlabs.axelix.master.utils.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.ClassTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.master.domain.database.CommunityRDBMS;

/**
 * Composed annotation to demarcate the given integration test as the one, where all the test methods
 * must run against all the {@link CommunityRDBMS community supported databases}.
 * <p>
 * There is a very important implementation note here.
 * <p>
 * A {@link ClassTemplate} boots and caches a <em>single</em> Spring context for the whole class, shared across all
 * database invocations. {@link DataSourceSetupExtension} therefore does two things per invocation: before the
 * invocation it sets {@code spring.datasource.url} (and credentials) for the target database so the context is wired
 * to it, and after the invocation it evicts the cached context so the next database boots a fresh context and
 * re-evaluates the {@code @ConditionalOnCommunityRdbms} conditions. See {@link DataSourceSetupExtension} for the exact
 * mechanism and why {@code @DirtiesContext} cannot be used here (its callbacks fire once for the whole template, not
 * per invocation).
 *
 * @author Mikhail Polivakha
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ClassTemplate
@ExtendWith(DataSourceSetupExtension.class)
@SpringBootTest
public @interface DatabaseMatrixTest {}
