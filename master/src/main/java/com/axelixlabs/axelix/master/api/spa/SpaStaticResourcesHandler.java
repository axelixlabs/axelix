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
package com.axelixlabs.axelix.master.api.spa;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axelixlabs.axelix.master.autoconfiguration.web.WebProperties;

/**
 * And endpoint that is supposed to serve the SPA related files (js,css,html).
 *
 * @author Mikhail Polivakha
 */
@RestController
public class SpaStaticResourcesHandler {

    private final WebProperties webProperties;
    private final Resource indexHtmlLocation;
    private static final Map<String, MediaType> MEDIA_TYPES_CACHE;

    static {
        // It is okay to just use HashMap here, there is no writing under contention here.
        MEDIA_TYPES_CACHE = new HashMap<>(4, 1.1f);
        MEDIA_TYPES_CACHE.put("js", MediaType.parseMediaType("text/javascript"));
        MEDIA_TYPES_CACHE.put("css", MediaType.parseMediaType("text/css"));
        MEDIA_TYPES_CACHE.put("html", MediaType.parseMediaType("text/html"));
        MEDIA_TYPES_CACHE.put("htm", MediaType.parseMediaType("text/html"));
    }

    public SpaStaticResourcesHandler(WebProperties webProperties) throws IOException {
        this.webProperties = webProperties;
        this.indexHtmlLocation = webProperties.getLocation().createRelative("index.html");
    }

    // TODO: Add ETag caching support
    @GetMapping(value = "/**")
    public ResponseEntity<Resource> getResource(HttpServletRequest request) throws IOException {
        String contextPath = request.getServletPath();

        Resource resolvedResource = resolveResourceToReturn(contextPath);

        String fileExtension = getFileExtension(resolvedResource);

        MediaType mediaType = MEDIA_TYPES_CACHE.get(fileExtension);

        return ResponseEntity.status(HttpStatus.OK).contentType(mediaType).body(resolvedResource);
    }

    // yeah, that's right, for some goddamn reason null-away STILL thinks that assert null check is not sufficient
    @SuppressWarnings("NullAway")
    private static String getFileExtension(Resource resolvedResource) {
        String filename = resolvedResource.getFilename();

        Assert.notNull(filename, "The filename of the static resource cannot be null");

        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private Resource resolveResourceToReturn(String contextPath) throws IOException {
        Resource relative = webProperties.getLocation().createRelative(contextPath);

        return relative.exists() && relative.isReadable() ? relative : indexHtmlLocation;
    }
}
