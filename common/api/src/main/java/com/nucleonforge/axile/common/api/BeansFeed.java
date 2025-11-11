package com.nucleonforge.axile.common.api;

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

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to beans actuator endpoint.
 *
 * @author Mikhail Polivakha
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/beans.html">Beans Endpoint</a>
 * @see ActuatorEndpoint
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
        UNKNOWN
    }

    public sealed interface BeanSource permits BeanMethod, ComponentVariant, FactoryBean, UnknownBean {

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
    public record BeanMethod(@Nullable String enclosingClassName, String methodName) implements BeanSource {

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
