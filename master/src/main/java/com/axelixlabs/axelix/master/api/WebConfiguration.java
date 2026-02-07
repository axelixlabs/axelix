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
package com.axelixlabs.axelix.master.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.internal.InternalApiRestController;

/**
 * Web layer related configuration.
 *
 * @author Mikhail Polivakha
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    // /api/external <-- api external
    // /api/internal <-- api internal
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {

        // prefixes for various types of API
        configurer.addPathPrefix("/api/external", HandlerTypePredicate.forAnnotation(ExternalApiRestController.class));
        configurer.addPathPrefix("/api/internal", HandlerTypePredicate.forAnnotation(InternalApiRestController.class));
    }
}
