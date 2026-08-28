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
package com.axelixlabs.axelix.master.service.auth.provider;

import java.util.Objects;
import java.util.Set;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.AuthoritiesManager;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.SuperAdminConfigurationProperties;
import com.axelixlabs.axelix.master.service.auth.encoder.SuperAdminPasswordEncoder;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;

/**
 * {@link UserAuthenticator} that authenticates Super Admin.
 *
 * @author Mikhail Polivakha
 * @author Ilya Naumov
 */
public class SuperAdminUserAuthenticator implements UserAuthenticator {

    public static final String SUPER_ADMIN_ROLE_NAME = "SUPER_ADMIN";

    private final SuperAdminConfigurationProperties superAdminConfiguration;
    private final SuperAdminPasswordEncoder passwordEncoder;
    private final AuthoritiesManager authoritiesManager;

    public SuperAdminUserAuthenticator(
            SuperAdminConfigurationProperties superAdminConfiguration,
            SuperAdminPasswordEncoder passwordEncoder,
            AuthoritiesManager authoritiesManager) {
        this.superAdminConfiguration = superAdminConfiguration;
        this.passwordEncoder = passwordEncoder;
        this.authoritiesManager = authoritiesManager;
    }

    @PostConstruct
    public void validate() {
        passwordEncoder.validatePasswordFormat(superAdminConfiguration.getPassword());
    }

    @Override
    public @Nullable User authenticate(String username, String password) {

        if (Objects.equals(superAdminConfiguration.getUsername(), username)
                && passwordEncoder.matches(password, superAdminConfiguration.getPassword())) {

            return new DefaultUser(
                    superAdminConfiguration.getUsername(),
                    passwordEncoder.extractEncodedPassword(superAdminConfiguration.getPassword()),
                    Set.of(new DefaultRole(SUPER_ADMIN_ROLE_NAME, authoritiesManager.getAll())));
        }

        return null;
    }
}
