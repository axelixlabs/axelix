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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtSerializer;
import com.axelixlabs.axelix.sbs.spring.core.config.AuthProperties;

/**
 * JwtAuth test configuration.
 *
 * @author Nikita Kirillov
 */
@TestConfiguration
public class JwtAuthTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = AuthProperties.CONFIG_PROPS_PREFIX)
    public AuthProperties authProperties() {
        return new AuthProperties();
    }

    @Bean
    public JwtEncoderService jwtEncoderService(AuthProperties authProperties) {
        return new DefaultJwtEncoderService(
                authProperties.getJwt().getAlgorithm(), authProperties.getJwt().getSigningKey(), Duration.ofHours(1));
    }

    @Bean
    public TechUserJwtSerializer techUserJwtSerializer(AuthProperties authProperties) {
        return new DefaultTechUserJwtSerializer(
                authProperties.getJwt().getAlgorithm(), authProperties.getJwt().getSigningKey());
    }

    @Bean
    public TechUserJwtDeserializer techUserJwtDeserializer(AuthProperties authProperties) {
        return new DefaultTechUserJwtDeserializer(
                authProperties.getJwt().getAlgorithm(), authProperties.getJwt().getSigningKey());
    }

    @Bean
    public WebIdentityAccessManager securityManager(TechUserJwtDeserializer techUserJwtDeserializer) {
        return new ManagedServiceWebIdentityAccessManager(techUserJwtDeserializer);
    }
}
