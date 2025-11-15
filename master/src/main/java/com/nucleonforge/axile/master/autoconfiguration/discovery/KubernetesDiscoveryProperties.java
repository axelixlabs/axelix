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
    private String tokenPath = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    /**
     * The path inside the K8S Axile Master pod where the certificate of the kube-apiserver resides.
     */
    private String caCertFile = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";

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

    public String getTokenPath() {
        return tokenPath;
    }

    public KubernetesDiscoveryProperties setTokenPath(String tokenPath) {
        this.tokenPath = tokenPath;
        return this;
    }

    public String getCaCertFile() {
        return caCertFile;
    }

    public KubernetesDiscoveryProperties setCaCertFile(String caCertFile) {
        this.caCertFile = caCertFile;
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
