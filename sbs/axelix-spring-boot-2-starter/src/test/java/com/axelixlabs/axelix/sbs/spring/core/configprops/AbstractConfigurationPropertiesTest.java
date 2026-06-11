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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for the configprops-related integration tests.
 *
 * <p>Every beans test extends this class and declares no context-affecting annotations of its own
 * (no {@code @SpringBootTest}, {@code @TestPropertySource}, {@code @Import} or nested
 * {@code @TestConfiguration} classes). As a result, all the configprops tests produce an identical
 * merged context configuration and therefore share a single cached Spring application context,
 * which is only started once for the whole test run.
 *
 * <p>{@link AxelixConfigurationPropertiesEndpointTest} is intentionally not part of this hierarchy.
 *
 * <p>The annotations below are the union of the configuration previously declared by the
 * individual configprops tests.
 *
 * @author Artemiy Degtyarev
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "management.endpoint.configprops.show-values=always"
)
@TestPropertySource(
    properties = {
        "axelix.prop.test.tags.environment=test",
        "axelix.prop.test.tags.version=1.0.0",
        "axelix.prop.test.enabled-contexts=user-service,payment-service",
        "axelix.prop.test.http-client.requests[0].name=user-api",
        "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
        "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
        "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
        "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
        "axelix.prop.test.http-client.requests[0].methods[1].type=POST",
        "axelix.prop.test.tags.forSanitization=toBeSanitized",
        "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized"
    }
)
@Import({AbstractConfigurationPropertiesTest.ConfigurationPropertiesTestConfiguration.class})
@EnableConfigurationProperties(AbstractConfigurationPropertiesTest.AxelixConfigurationProperties.class)
public abstract class AbstractConfigurationPropertiesTest {
    @ConstructorBinding
    @ConfigurationProperties(prefix = "axelix.prop.test")
    public static final class AxelixConfigurationProperties {
        private final Map<String, String> tags;
        private final List<String> enabledContexts;
        private final HttpClient httpClient;

        public AxelixConfigurationProperties(
            Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {
            this.tags = tags;
            this.enabledContexts = enabledContexts;
            this.httpClient = httpClient;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public List<String> getEnabledContexts() {
            return enabledContexts;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (AxelixConfigurationProperties) obj;
            return Objects.equals(this.tags, that.tags)
                && Objects.equals(this.enabledContexts, that.enabledContexts)
                && Objects.equals(this.httpClient, that.httpClient);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tags, enabledContexts, httpClient);
        }

        @Override
        public String toString() {
            return "AxelixConfigurationProperties[" + "tags="
                + tags + ", " + "enabledContexts="
                + enabledContexts + ", " + "httpClient="
                + httpClient + ']';
        }

        @ConstructorBinding
        public static final class HttpClient {
            private final List<Request> requests;

            public HttpClient(List<Request> requests) {
                this.requests = requests;
            }

            public List<Request> getRequests() {
                return requests;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (HttpClient) obj;
                return Objects.equals(this.requests, that.requests);
            }

            @Override
            public int hashCode() {
                return Objects.hash(requests);
            }

            @Override
            public String toString() {
                return "HttpClient[" + "requests=" + requests + ']';
            }
        }

        @ConstructorBinding
        public static final class Request {
            private final String name;
            private final String baseUrl;
            private final List<Method> methods;

            public Request(String name, String baseUrl, List<Method> methods) {
                this.name = name;
                this.baseUrl = baseUrl;
                this.methods = methods;
            }

            public String getName() {
                return name;
            }

            public String getBaseUrl() {
                return baseUrl;
            }

            public List<Method> getMethods() {
                return methods;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Request) obj;
                return Objects.equals(this.name, that.name)
                    && Objects.equals(this.baseUrl, that.baseUrl)
                    && Objects.equals(this.methods, that.methods);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, baseUrl, methods);
            }

            @Override
            public String toString() {
                return "Request[" + "name=" + name + ", " + "baseUrl=" + baseUrl + ", " + "methods=" + methods + ']';
            }
        }

        @ConstructorBinding
        public static final class Method {
            private final String type;
            private final List<Retry> retries;

            public Method(String type, List<Retry> retries) {
                this.type = type;
                this.retries = retries;
            }

            public String getType() {
                return type;
            }

            public List<Retry> getRetries() {
                return retries;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Method) obj;
                return Objects.equals(this.type, that.type) && Objects.equals(this.retries, that.retries);
            }

            @Override
            public int hashCode() {
                return Objects.hash(type, retries);
            }

            @Override
            public String toString() {
                return "Method[" + "type=" + type + ", " + "retries=" + retries + ']';
            }
        }

        @ConstructorBinding
        public static final class Retry {
            private final Integer count;
            private final Map<String, Object> parameters;

            public Retry(Integer count, Map<String, Object> parameters) {
                this.count = count;
                this.parameters = parameters;
            }

            public Integer getCount() {
                return count;
            }

            public Map<String, Object> getParameters() {
                return parameters;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Retry) obj;
                return Objects.equals(this.count, that.count) && Objects.equals(this.parameters, that.parameters);
            }

            @Override
            public int hashCode() {
                return Objects.hash(count, parameters);
            }

            @Override
            public String toString() {
                return "Retry[" + "count=" + count + ", " + "parameters=" + parameters + ']';
            }
        }
    }

    @TestConfiguration
    static class ConfigurationPropertiesTestConfiguration {
        @Bean
        public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
            return new DefaultConfigurationPropertiesFlattener();
        }

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter(
            ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
            return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
        }

        @Bean
        @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
        public EndpointsConfigurationProperties endpointsConfigurationProperties() {
            return new EndpointsConfigurationProperties();
        }

        @Bean
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        public SecurityContextExecutor securityContextExecutor() {
            return new ThreadLocalSecurityContextExecutor();
        }

        @Bean
        public RequiredAuthorityCheckService requiredAuthorityCheckService(
            SecurityContextExecutor securityContextExecutor) {
            return new RequiredAuthorityCheckService(securityContextExecutor);
        }

        @Bean(name = "configurationPropertiesServiceAll")
        public ConfigurationPropertiesService configurationPropertiesServiceAll(
            @Qualifier("sanitizeAll") SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
            return new DefaultConfigurationPropertiesService(
                smartSanitizingFunction,
                applicationContext,
                configurationPropertiesConverter,
                requiredAuthorityCheckService);
        }

        @Bean(name = "sanitizeAll")
        public SmartSanitizingFunction smartSanitizingFunctionAll(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
        }

        @Bean(name = "configurationPropertiesServiceExplicit")
        public ConfigurationPropertiesService configurationPropertiesServiceExplicit(
            @Qualifier("sanitizeExplicit") SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
            return new DefaultConfigurationPropertiesService(
                smartSanitizingFunction,
                applicationContext,
                configurationPropertiesConverter,
                requiredAuthorityCheckService);
        }

        @Bean(name = "sanitizeExplicit")
        public SmartSanitizingFunction smartSanitizingFunctionExplicit(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(
                List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                propertyNameNormalizer);
        }

        @Bean
        public ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint() {
            return new ConfigurationPropertiesReportEndpoint(List.of());
        }
    }
}
