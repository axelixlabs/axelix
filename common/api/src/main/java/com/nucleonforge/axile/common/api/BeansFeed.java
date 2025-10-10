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
            String scope,
            String type,
            ProxyType proxyType,
            Set<String> aliases,
            Set<String> dependencies,
            boolean isLazyInit,
            boolean isPrimary,
            List<String> qualifiers,
            BeanSource beanSource) {
        @JsonCreator
        public Bean(
                @JsonProperty("scope") String scope,
                @JsonProperty("type") String type,
                @JsonProperty("proxyType") ProxyType proxyType,
                @JsonProperty("aliases") Set<String> aliases,
                @JsonProperty("dependencies") Set<String> dependencies,
                @JsonProperty("isLazyInit") boolean isLazyInit,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("qualifiers") List<String> qualifiers,
                @JsonDeserialize(using = BeanSourceDeserializer.class) BeanSource beanSource) {
            this.scope = scope;
            this.type = type;
            this.proxyType = proxyType;
            this.aliases = aliases != null ? aliases : Collections.emptySet();
            this.dependencies = dependencies != null ? dependencies : Collections.emptySet();
            this.isLazyInit = isLazyInit;
            this.isPrimary = isPrimary;
            this.qualifiers = qualifiers != null ? qualifiers : Collections.emptyList();
            this.beanSource = beanSource;
        }
    }

    public enum BeanOrigin {
        COMPONENT_ANNOTATION,
        BEAN_METHOD,
        FACTORY_BEAN,
        UNKNOWN
    }

    public sealed interface BeanSource permits BeanMethod, ComponentVariant, FactoryBean, UnknownBean {
        @JsonGetter("origin")
        BeanOrigin origin();
    }

    @JsonIgnoreProperties("origin")
    public record UnknownBean() implements BeanSource {
        @Override
        public BeanOrigin origin() { return BeanOrigin.UNKNOWN; }
    }

    @JsonIgnoreProperties("origin")
    public record FactoryBean(String factoryBeanName) implements BeanSource {
        @Override
        public BeanOrigin origin() { return BeanOrigin.FACTORY_BEAN; }
    }

    @JsonIgnoreProperties("origin")
    public record BeanMethod(String enclosingClassName, String methodName) implements BeanSource {
        @Override
        public BeanOrigin origin() { return BeanOrigin.BEAN_METHOD; }
    }

    @JsonIgnoreProperties("origin")
    public record ComponentVariant() implements BeanSource {
        @Override
        public BeanOrigin origin() { return BeanOrigin.COMPONENT_ANNOTATION; }
    }

    public enum ProxyType {
        JDK_PROXY,
        CGLIB,
        NO_PROXYING
    }
}
