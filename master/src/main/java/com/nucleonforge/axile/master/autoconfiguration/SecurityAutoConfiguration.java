package com.nucleonforge.axile.master.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.nucleonforge.axile.master.service.auth.jwt.DefaultJwtEncoderService;
import com.nucleonforge.axile.master.service.auth.jwt.JwtEncoderService;
import com.nucleonforge.axile.master.service.auth.provider.StaticAdminUserProvider;

/**
 * Autoconfiguration for security.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    /**
     * Autoconfiguration for the JWT-related part.
     */
    @AutoConfiguration
    public static class JwtAutoConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axile.master.auth.jwt")
        JwtProperties jwtProperties() {
            return new JwtProperties();
        }

        @Bean
        JwtEncoderService jwtEncoderService(JwtProperties jwtProperties) {
            return new DefaultJwtEncoderService(
                    jwtProperties.getAlgorithm(), jwtProperties.getSigningKey(), jwtProperties.getLifespan());
        }
    }

    /**
     * Autoconfiguration for static-admin security option.
     */
    @AutoConfiguration
    @ConditionalOnProperty(prefix = "axile.master.auth", name = "type", havingValue = "static-admin")
    static class StaticCredentialsConfig {

        private static final String USERNAME_NULL_MESSAGE =
                "The username for the static-admin is 'null'. Make sure the axile.master.auth.static-admin.credentials.username is specified correctly";
        private static final String PASSWORD_NULL_MESSAGE =
                "The password for the static-admin is 'null'. Make sure the axile.master.auth.static-admin.credentials.password is specified correctly";

        @Bean
        public StaticAdminUserProvider staticCredentialsUserProvider(
                StaticAdminCredentialsProperties staticCredentialsConfig) {
            Assert.notNull(staticCredentialsConfig.getUsername(), USERNAME_NULL_MESSAGE);
            Assert.notNull(staticCredentialsConfig.getPassword(), PASSWORD_NULL_MESSAGE);

            return new StaticAdminUserProvider(staticCredentialsConfig);
        }

        @Bean
        @ConfigurationProperties(prefix = "axile.master.auth.static-admin.credentials")
        public StaticAdminCredentialsProperties staticAdminCredentialsProperties() {
            return new StaticAdminCredentialsProperties();
        }
    }
}
