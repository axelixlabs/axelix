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
package com.axelixlabs.axelix.master.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.master.autoconfiguration.web.WebProperties;

/**
 * Filter that is designed to handle the static SPA resources.
 *
 * @author Mikhail Polivakha
 */
public class SpaStaticResourcesServingFilter extends OncePerRequestFilter {

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

    public SpaStaticResourcesServingFilter(WebProperties webProperties) throws IOException {
        this.webProperties = webProperties;
        this.indexHtmlLocation = webProperties.getLocation().createRelative("index.html");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        String contextPath = request.getServletPath();

        Resource resolvedResource = resolveResourceToReturn(contextPath);

        String fileExtension = getFileExtension(resolvedResource);

        MediaType mediaType = MEDIA_TYPES_CACHE.get(fileExtension);
        if (mediaType != null) {
            response.setContentType(mediaType.toString());
        }

        response.setStatus(HttpStatus.OK.value());
        response.getOutputStream().write(resolvedResource.getContentAsByteArray());
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

        try {
            return relative.exists() && relative.isReadable() ? relative : indexHtmlLocation;
        } catch (IllegalArgumentException e) { // spring throws in certain scenarios
            return indexHtmlLocation;
        }
    }
}
