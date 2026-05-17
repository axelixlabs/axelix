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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;

/**
 * Integration test for {@link QualifiersPersistencePostProcessor}.
 *
 * @author Mikhail Polivakha
 */
class QualifiersPersistencePostProcessorTest extends AbstractEndpointTest {

    @Test
    void shouldDetectAnnotationQualifiers() {
        DefaultQualifiersRegistry registry = DefaultQualifiersRegistry.INSTANCE;

        SoftAssertions.assertSoftly(sa -> {
            sa.assertThat(registry.getQualifiers("noQualifiersBeanName")).isEmpty();
            sa.assertThat(registry.getQualifiers("builtInTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier");
            sa.assertThat(registry.getQualifiers("customTypeQualifierBeanName")).containsOnly("customQualifier");
            sa.assertThat(registry.getQualifiers("mixedTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier", "customQualifier");
        });

        SoftAssertions.assertSoftly(sa -> {
            sa.assertThat(registry.getQualifiers("beanMethodNoQualifiersBeanName"))
                    .isEmpty();
            sa.assertThat(registry.getQualifiers("beanMethodBuiltInTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier");
            sa.assertThat(registry.getQualifiers("beanMethodCustomTypeQualifierBeanName"))
                    .containsOnly("customQualifier");
            sa.assertThat(registry.getQualifiers("beanMethodMixedTypeQualifierBeanName"))
                    .containsOnly("builtInTypeQualifier", "customQualifier");
        });

        SoftAssertions.assertSoftly(sa -> {
            sa.assertThat(registry.getQualifiers("noQualifiersConfigBeanName")).isEmpty();
            sa.assertThat(registry.getQualifiers("builtInTypeQualifierConfigBeanName"))
                    .containsOnly("builtInTypeQualifier");
            sa.assertThat(registry.getQualifiers("customTypeQualifierConfigBeanName"))
                    .containsOnly("customQualifier");
            sa.assertThat(registry.getQualifiers("mixedTypeQualifierConfigBeanName"))
                    .containsOnly("builtInTypeQualifier", "customQualifier");
        });
    }
}
