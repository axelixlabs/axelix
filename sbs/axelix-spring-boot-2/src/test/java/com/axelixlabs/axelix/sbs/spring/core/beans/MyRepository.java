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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Test-only Spring Data JPA repository backing {@link DefaultBeanMetaInfoExtractorTest}. Auto-detected
 * by the default {@code @EnableJpaRepositories} scan rooted at the {@code @SpringBootApplication}
 * package, which makes the {@code MyRepository} bean (proxied by {@code JpaRepositoryFactoryBean})
 * available in the shared endpoint context.
 *
 * @author Mikhail Polivakha
 */
@Repository(MyRepository.BEAN_NAME)
public interface MyRepository extends JpaRepository<MyEntity, Long> {

    String BEAN_NAME = "MyRepository";
}
