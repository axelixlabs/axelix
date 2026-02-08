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

import java.io.IOException;

import org.jspecify.annotations.NonNull;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * The specific {@link PathResourceResolver} that is designed to fall back to
 * index.html in case there is no requested resource bundle (javascript or css)
 * found.
 * <p>
 * The need for the customized {@link PathResourceResolver} emerges from the fact
 * that Spring's Web internal web server needs to be able to handle the front-end
 * bundles (javascript, css) for SPA applications, that have only one single index.html
 * page that is built up by dynamic javascript bundles.
 *
 * @author Mikhail Polivakha
 */
public final class SpaBundlesPathResourceResolver extends PathResourceResolver {

    private final Resource indexHtmlPath;

    /**
     * @param indexHtml {@link Resource} that represents {@code index.html}.
     */
    public SpaBundlesPathResourceResolver(Resource indexHtml) {
        this.indexHtmlPath = indexHtml;
    }

    @Override
    protected Resource getResource(@NonNull String resourcePath, Resource location) throws IOException {

        Resource relative = location.createRelative(resourcePath);

        return relative.exists() && relative.isReadable() ? relative : indexHtmlPath;
    }
}
