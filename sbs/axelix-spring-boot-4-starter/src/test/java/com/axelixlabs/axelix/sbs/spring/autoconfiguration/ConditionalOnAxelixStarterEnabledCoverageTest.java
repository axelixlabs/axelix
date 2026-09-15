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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Verifies that every auto-configuration contributed by this starter is annotated with
 * {@link ConditionalOnAxelixStarterEnabled}.
 *
 * @author Nikita Kirillov
 */
class ConditionalOnAxelixStarterEnabledCoverageTest {

    private static final JavaClasses AUTOCONFIGURATION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.axelixlabs.axelix.sbs.spring.autoconfiguration");

    @Test
    void everyAutoConfigurationCarriesTheKillSwitchAnnotation() {
        classes()
                .that()
                .areAnnotatedWith(AutoConfiguration.class)
                .should()
                .beMetaAnnotatedWith(ConditionalOnAxelixStarterEnabled.class)
                .because("axelix.sbs.enabled=false won't actually disable an auto-configuration without it")
                .allowEmptyShould(false)
                .check(AUTOCONFIGURATION_CLASSES);
    }
}
