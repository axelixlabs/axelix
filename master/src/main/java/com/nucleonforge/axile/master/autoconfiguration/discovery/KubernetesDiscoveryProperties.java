/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.autoconfiguration.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Properties related to autodiscovery in K8S environments
 *
 * @see DiscoveryAutoConfiguration.KubernetesDiscoveryAutoConfiguration
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
public class KubernetesDiscoveryProperties {

    /**
     * URL of the kube-apiserver to be used by Axile master when discovering
     * the potentially managed services.
     */
    private String kubeApiserverUrl;

    /**
     * The path inside the K8S Axile Master pod where the Service Account token resides.
     */
    private String saTokenPath = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    /**
     * The path inside the K8S Axile Master pod where the certificate of the kube-apiserver resides.
     */
    private String caCertPath = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";

    private DiscoveryFilters filters;

    /**
     * Filters to be applied during discovery of managed services.
     *
     * @author Mikhail Polivakha
     */
    public static class DiscoveryFilters {

        /**
         *
         */
        private Set<String> namespaces;

        /**
         * Labels that are used for filtering of the
         */
        private Map<String, String> labels = new HashMap<>();

        public Set<String> getNamespaces() {
            return namespaces;
        }

        public DiscoveryFilters setNamespaces(Set<String> namespaces) {
            this.namespaces = namespaces;
            return this;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public DiscoveryFilters setLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }
    }

    public String getKubeApiserverUrl() {
        return kubeApiserverUrl;
    }

    public KubernetesDiscoveryProperties setKubeApiserverUrl(String kubeApiserverUrl) {
        this.kubeApiserverUrl = kubeApiserverUrl;
        return this;
    }

    public String getSaTokenPath() {
        return saTokenPath;
    }

    public KubernetesDiscoveryProperties setSaTokenPath(String saTokenPath) {
        this.saTokenPath = saTokenPath;
        return this;
    }

    public String getCaCertPath() {
        return caCertPath;
    }

    public KubernetesDiscoveryProperties setCaCertPath(String caCertPath) {
        this.caCertPath = caCertPath;
        return this;
    }

    public DiscoveryFilters getFilters() {
        return filters;
    }

    public KubernetesDiscoveryProperties setFilters(DiscoveryFilters filters) {
        this.filters = filters;
        return this;
    }
}
