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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response to axelix-configprops actuator endpoint.
 *
 * @author Sergey Cherkasov
 */
public final class ConfigPropsFeed {

    private final Map<String, Context> contexts;

    /**
     * Creates a new ConfigPropsFeed.
     *
     * @param contexts The application contexts keyed by context id.
     */
    public ConfigPropsFeed(@JsonProperty("contexts") Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigPropsFeed that = (ConfigPropsFeed) o;
        return Objects.equals(contexts, that.contexts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contexts);
    }

    @Override
    public String toString() {
        return "ConfigPropsFeed{" + "contexts=" + contexts + '}';
    }

    /**
     * DTO that encapsulates the context of the given artifact.
     */
    public static final class Context {

        private final Map<String, Bean> beans;
        private final String parentId;

        /**
         * Creates a new Context.
         *
         * @param beans    The unified map of beans that contains beans from one or more contexts.
         *                 The key is the bean name (with potentially stripped config-props prefix), value is the profile of the given bean.
         * @param parentId The id of the parent application context, if any.
         */
        public Context(@JsonProperty("beans") Map<String, Bean> beans, @JsonProperty("parentId") String parentId) {
            this.beans = beans;
            this.parentId = parentId;
        }

        public Map<String, Bean> getBeans() {
            return beans;
        }

        public String getParentId() {
            return parentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Context context = (Context) o;
            return Objects.equals(beans, context.beans) && Objects.equals(parentId, context.parentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(beans, parentId);
        }

        @Override
        public String toString() {
            return "Context{" + "beans=" + beans + ", parentId='" + parentId + '\'' + '}';
        }
    }

    /**
     * DTO that encapsulates the {@code @ConfigurationProperties} bean of the given artifact.
     */
    public static final class Bean {

        private final String prefix;
        private final List<KeyValue> properties;
        private final List<KeyValue> inputs;

        /**
         * Creates a new Bean.
         *
         * @param prefix     The prefix applied to the names of the bean properties.
         * @param properties The properties of the bean as name-value pairs.
         * @param inputs     The origin and value of each configuration parameter
         *                   — which value was applied and from which source
         *                   — to configure a specific property.
         */
        @JsonCreator
        public Bean(
                @JsonProperty("prefix") String prefix,
                @JsonProperty("properties") List<KeyValue> properties,
                @JsonProperty("inputs") List<KeyValue> inputs) {
            this.prefix = prefix;
            this.properties = properties;
            this.inputs = inputs;
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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bean bean = (Bean) o;
            return Objects.equals(prefix, bean.prefix)
                    && Objects.equals(properties, bean.properties)
                    && Objects.equals(inputs, bean.inputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix, properties, inputs);
        }

        @Override
        public String toString() {
            return "Bean{" + "prefix='" + prefix + '\'' + ", properties=" + properties + ", inputs=" + inputs + '}';
        }
    }
}
