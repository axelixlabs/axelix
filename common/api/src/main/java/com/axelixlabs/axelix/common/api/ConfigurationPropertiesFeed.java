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
package com.axelixlabs.axelix.common.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The feed of {@code @ConfigurationProperties} beans used in the application.
 *
 * @author Sergey Cherkasov
 */
public final class ConfigurationPropertiesFeed {

    private final List<ConfigurationProperties> beans;

    /**
     * Create new ConfigPropsFeed.
     *
     * @param beans  The unified list of beans that contains beans from one or more contexts.
     */
    @JsonCreator
    public ConfigurationPropertiesFeed(@JsonProperty("beans") List<ConfigurationProperties> beans) {
        this.beans = beans != null ? beans : Collections.emptyList();
    }

    public List<ConfigurationProperties> getBeans() {
        return beans;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigurationPropertiesFeed that = (ConfigurationPropertiesFeed) o;
        return Objects.equals(beans, that.beans);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(beans);
    }

    @Override
    public String toString() {
        return "ConfigPropsFeed{" + "beans=" + beans + '}';
    }

    /**
     * The profile of a given {@code @ConfigurationProperties} bean.
     *
     * @author Sergey Cherkasov
     */
    public static final class ConfigurationProperties {
        private final String beanName;
        private final String prefix;
        private final List<KeyValue> properties;
        private final List<KeyValue> inputs;

        /**
         * Create new ConfigProps.
         *
         * @param beanName     The name of the bean.
         * @param prefix       The prefix applied to the names of the bean properties.
         * @param properties   The properties of the bean as name-value pairs.
         * @param inputs       The origin and value of each configuration parameter
         *                     — which value was applied and from which source
         *                     — to configure a specific property.
         */
        @JsonCreator
        public ConfigurationProperties(
                @JsonProperty("beanName") String beanName,
                @JsonProperty("prefix") String prefix,
                @JsonProperty("properties") List<KeyValue> properties,
                @JsonProperty("inputs") List<KeyValue> inputs) {
            this.beanName = beanName;
            this.prefix = prefix;
            this.properties = properties;
            this.inputs = inputs;
        }

        public String getBeanName() {
            return beanName;
        }

        public String getPrefix() {
            return prefix;
        }

        public List<KeyValue> getProperties() {
            return properties;
        }

        public List<KeyValue> getInputs() {
            return inputs;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConfigurationProperties that = (ConfigurationProperties) o;
            return Objects.equals(beanName, that.beanName)
                    && Objects.equals(prefix, that.prefix)
                    && Objects.equals(properties, that.properties)
                    && Objects.equals(inputs, that.inputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(beanName, prefix, properties, inputs);
        }

        @Override
        public String toString() {
            return "ConfigProps{" + "beanName='"
                    + beanName + '\'' + ", prefix='"
                    + prefix + '\'' + ", properties="
                    + properties + ", inputs="
                    + inputs + '}';
        }
    }
}
