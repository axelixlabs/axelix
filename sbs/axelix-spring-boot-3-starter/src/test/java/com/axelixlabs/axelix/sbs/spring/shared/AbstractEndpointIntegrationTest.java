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
package com.axelixlabs.axelix.sbs.spring.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;

/**
 * Base class for all Axelix actuator endpoint integration tests.
 * <p>
 * It is the single carrier of the {@link SpringBootTest} annotation across every endpoint test, so that all
 * subclasses resolve to one and the same Spring {@code TestContext} cache key and therefore share a single
 * {@code ApplicationContext} (one {@code RANDOM_PORT} web server is started for the whole suite instead of one
 * per test class). Subclasses MUST NOT add anything that changes the merged context configuration (no
 * {@code @SpringBootTest}, {@code @Import}, {@code @TestPropertySource}, {@code properties}, {@code args},
 * {@code @EnableConfigurationProperties}, {@code @DynamicPropertySource} or nested {@code @Configuration}); all
 * shared beans live in {@link SharedEndpointTestConfiguration} and all shared properties live in
 * {@code application.yaml}.
 *
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {com.axelixlabs.axelix.sbs.spring.core.Main.class, SharedEndpointTestConfiguration.class})
@TestPropertySource("classpath:shared-endpoint-test.properties")
public abstract class AbstractEndpointIntegrationTest {

    @Autowired
    protected TestRestTemplateBuilder testRestTemplate;
}
