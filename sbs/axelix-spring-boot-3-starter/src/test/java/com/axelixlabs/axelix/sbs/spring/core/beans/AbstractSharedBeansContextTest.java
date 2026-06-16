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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.sbs.spring.core.Main;

/**
 * Shared base test that defines a single Spring {@link org.springframework.context.ApplicationContext}
 * reused by the non-endpoint "beans" tests so they hit the Spring TestContext cache instead of each
 * building its own context.
 *
 * <p>{@link Main} is pinned via the {@code classes} attribute (rather than left to Spring Boot's
 * auto-detection) so both subclasses resolve an identical, deterministic configuration — this is
 * what makes the cached context shareable, and it also keeps auto-configuration (e.g. the embedded
 * {@code DataSource}) active.
 *
 * @author Artemiy Degtyarev
 */
@SpringBootTest(classes = Main.class)
@Import({
    DefaultBeanMetaInfoExtractorTest.DefaultBeanAnalyzerTestConfig.class,
    QualifiersPersistencePostProcessorTest.BeanMethodDeclarations.class,
    QualifiersPersistencePostProcessorTest.ComponentMethodDeclarations.class,
    QualifiersPersistencePostProcessorTest.ConfigurationClassesDeclarations.class
})
abstract class AbstractSharedBeansContextTest {}
