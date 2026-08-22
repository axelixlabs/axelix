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
package com.axelixlabs.axelix.master.autoconfiguration.metrics;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Default handler for {@link PrometheusMetricsAutoConfiguration}'s {@code prometheusHttpServer}
 * bean: responds {@code 404} to any path not explicitly registered (e.g. {@code /} or
 * {@code /-/healthy} once its own handler is disabled), instead of the library's own default
 * handler, which answers {@code 200} on any unmatched path.
 *
 * @author Dmitry Mazurov
 */
public class WhitelabelNotFoundHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String body = "<html><body><h1>Whitelabel Error Page</h1>"
                    + "<p>This application has no explicit mapping for " + htmlEscape(path)
                    + ", so you are seeing this as a fallback.</p>"
                    + "<div id='created'>" + Instant.now() + "</div>"
                    + "<div>There was an unexpected error (type=Not Found, status=404).</div>"
                    + "</body></html>";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, bytes.length);
            exchange.getResponseBody().write(bytes);
        }
    }

    private static String htmlEscape(String input) {
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
