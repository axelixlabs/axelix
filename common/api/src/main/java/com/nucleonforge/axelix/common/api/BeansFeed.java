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
package com.nucleonforge.axelix.common.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to beans actuator endpoint.
 *
 * @see ActuatorEndpoint
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public record BeansFeed(Map<String, Context> contexts) {

    @JsonCreator
    public BeansFeed(@JsonProperty("contexts") Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    public record Context(String parentId, Map<String, Bean> beans) {

        @JsonCreator
        public Context(@JsonProperty("parentId") String parentId, @JsonProperty("beans") Map<String, Bean> beans) {
            this.parentId = parentId;
            this.beans = beans;
        }
    }

    public record Bean(
            @JsonProperty("scope") String scope,
            @JsonProperty("type") String type,
            @JsonProperty("proxyType") ProxyType proxyType,
            @JsonProperty("aliases") Set<String> aliases,
            @JsonProperty("autoConfigurationRef") @Nullable String autoConfigurationRef,
            @JsonProperty("dependencies") Set<BeanDependency> dependencies,
            @JsonProperty("isLazyInit") boolean isLazyInit,
            @JsonProperty("isPrimary") boolean isPrimary,
            @JsonProperty("isConfigPropsBean") boolean isConfigPropsBean,
            @JsonProperty("qualifiers") List<String> qualifiers,
            @JsonDeserialize(using = BeanSourceDeserializer.class) BeanSource beanSource) {

        public Bean {
            if (aliases == null) {
                aliases = Collections.emptySet();
            }

            if (dependencies == null) {
                dependencies = Collections.emptySet();
            }

            if (qualifiers == null) {
                qualifiers = Collections.emptyList();
            }
        }
    }

    public record BeanDependency(
            @JsonProperty("name") String name,
            @JsonProperty("isConfigPropsDependency") boolean isConfigPropsDependency) {}

    public enum BeanOrigin {
        COMPONENT_ANNOTATION,
        BEAN_METHOD,
        FACTORY_BEAN,
        SYNTHETIC_BEAN,
        UNKNOWN,
    }

    public sealed interface BeanSource permits BeanMethod, ComponentVariant, FactoryBean, SyntheticBean, UnknownBean {

        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        BeanOrigin origin();
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public record UnknownBean() implements BeanSource {

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.UNKNOWN;
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public record FactoryBean(String factoryBeanName) implements BeanSource {

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.FACTORY_BEAN;
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public record SyntheticBean() implements BeanSource {

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.SYNTHETIC_BEAN;
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public record BeanMethod(
            @Nullable String enclosingClassName, @Nullable String enclosingClassFullName, String methodName)
            implements BeanSource {

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.BEAN_METHOD;
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public record ComponentVariant() implements BeanSource {

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.COMPONENT_ANNOTATION;
        }
    }

    /**
     * The proxying approach that has been applied for the given bean.
     *
     * @author Nikita Kirillov
     */
    public enum ProxyType {
        JDK_PROXY,
        CGLIB,
        NO_PROXYING
    }
}
