package com.nucleonforge.axile.sbs.autoconfiguration.auth;

import io.jsonwebtoken.JwtParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;
import com.nucleonforge.axile.sbs.auth.filter.AuthorityResolver;
import com.nucleonforge.axile.sbs.auth.filter.DefaultAuthorityResolver;
import com.nucleonforge.axile.sbs.auth.filter.JwtAuthorizationFilter;
import com.nucleonforge.axile.sbs.auth.spi.Authorizer;
import com.nucleonforge.axile.sbs.auth.spi.DefaultAuthorizer;
import com.nucleonforge.axile.sbs.auth.spi.jwt.service.DefaultJwtDecoderService;
import com.nucleonforge.axile.sbs.auth.spi.jwt.service.JwtDecoderService;

/**
 * {@link AutoConfiguration} for JWT-based authentication support.
 * <p>
 * This configuration provides default beans for:
 * <ul>
 *   <li>{@link JwtDecoderService} — for decoding and restoring {@code User} objects from tokens.</li>
 *   <li>{@link AuthorityResolver} — to resolve required authorities based on request paths.</li>
 *   <li>{@link Authorizer} — to authorize {@code User} objects against authorization requests.</li>
 *   <li>{@link JwtAuthorizationFilter} — a servlet filter that enforces JWT authorization on incoming requests.</li>
 *   <li>{@link FilterRegistrationBean} for {@link JwtAuthorizationFilter} — registers the filter and restricts it to "/actuator/*" URL pattern.</li>
 * </ul>
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@AutoConfiguration
@ConditionalOnProperty(name = "axile.master.auth.jwt")
@ConditionalOnClass({JwtDecoderService.class, JwtParser.class})
public class JwtAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoderService jwtDecoderService(
            final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axile.master.auth.jwt.signing-key}") String signingKey) {
        return new DefaultJwtDecoderService(algorithm, signingKey);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorityResolver authorityResolver() {
        return new DefaultAuthorityResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public Authorizer authorizer() {
        return new DefaultAuthorizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthorizationFilter jwtAuthorizationFilter(
            JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        return new JwtAuthorizationFilter(jwtDecoderService, authorityResolver, authorizer);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
            JwtAuthorizationFilter filter) {
        FilterRegistrationBean<JwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setName("jwtAuthorizationFilter");
        registration.addUrlPatterns("/actuator/*");
        return registration;
    }
}
