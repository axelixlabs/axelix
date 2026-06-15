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
package com.axelixlabs.axelix.sbs.spring.core.utils;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Single test {@link ConfigurationProperties} class bound to the {@code axelix.prop.test} prefix.
 *
 * <p>It is shared between the endpoint integration tests that run in the common shared application
 * context, since registering several {@code @ConfigurationProperties} classes for the same prefix
 * would make assertions on the resolved config props bean ambiguous.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@ConfigurationProperties(prefix = "axelix.prop.test")
public class AxelixTestProperties {

    private String name;
    private Map<String, String> tags;
    private List<String> enabledContexts;
    private HttpClient httpClient;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public List<String> getEnabledContexts() {
        return enabledContexts;
    }

    public void setEnabledContexts(List<String> enabledContexts) {
        this.enabledContexts = enabledContexts;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static class HttpClient {

        private List<Request> requests;

        public List<Request> getRequests() {
            return requests;
        }

        public void setRequests(List<Request> requests) {
            this.requests = requests;
        }
    }

    public static class Request {

        private String name;
        private String baseUrl;
        private List<Method> methods;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public List<Method> getMethods() {
            return methods;
        }

        public void setMethods(List<Method> methods) {
            this.methods = methods;
        }
    }

    public static class Method {

        private String type;
        private List<Retry> retries;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Retry> getRetries() {
            return retries;
        }

        public void setRetries(List<Retry> retries) {
            this.retries = retries;
        }
    }

    public static class Retry {

        private Integer count;
        private Map<String, Object> parameters;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }
}
