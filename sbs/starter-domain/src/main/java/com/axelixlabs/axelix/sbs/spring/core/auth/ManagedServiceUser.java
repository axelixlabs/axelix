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

import java.util.Set;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.InternalAuthorities;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;

/**
 * The user that is supposed to be used inside the managed services (services
 * that run actual Spring Boot applications).
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public final class ManagedServiceUser implements User {

    public static final ManagedServiceUser STARTER_USER = new ManagedServiceUser();

    private ManagedServiceUser() {}

    @Override
    public String getUsername() {
        return "MANAGED_SERVICE";
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public Set<Role> getRoles() {
        return Set.of(new DefaultRole("MANAGED_SERVICE", Set.of(InternalAuthorities.SELF_REGISTER_AUTHORITY)));
    }
}
