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
package com.axelixlabs.axelix.common.auth.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.axelixlabs.axelix.common.auth.core.InternalAuthorities.SELF_REGISTER_AUTHORITY;

/**
 * Default {@link Role} backed by real {@link #authorities}.
 *
 * @see Role
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public final class DefaultRole implements Role {

    // TODO A task was created to remove ALL_ROLE_NAMES https://github.com/axelixlabs/axelix/issues/971
    public static final Set<String> ALL_ROLE_NAMES;
    public static final Role SUPER_ADMIN;
    public static final Role ADMIN;
    public static final Role EDITOR;
    public static final Role VIEWER;
    public static final Role MANAGED_SERVICE;

    static {
        VIEWER = new DefaultRole("VIEWER", Set.of());

        EDITOR = new DefaultRole(
                "EDITOR",
                Set.of(
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                        DefaultAuthority.CACHES_CLEAR,
                        DefaultAuthority.CACHES_TOGGLE,
                        DefaultAuthority.GARBAGE_COLLECTOR,
                        DefaultAuthority.THREAD_DUMP_TOGGLE));

        ADMIN = new DefaultRole(
                "ADMIN",
                Set.of(
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                        DefaultAuthority.CACHES_CLEAR,
                        DefaultAuthority.CACHES_TOGGLE,
                        DefaultAuthority.GARBAGE_COLLECTOR,
                        DefaultAuthority.THREAD_DUMP_TOGGLE,
                        DefaultAuthority.ENV_VALUES_READ,
                        DefaultAuthority.CONFIG_PROPS_VALUES_READ));

        SUPER_ADMIN = new DefaultRole(
                "SUPER_ADMIN", Arrays.stream(DefaultAuthority.values()).collect(Collectors.toUnmodifiableSet()));

        MANAGED_SERVICE = new DefaultRole("MANAGED_SERVICE", Set.of(SELF_REGISTER_AUTHORITY));

        ALL_ROLE_NAMES = Set.of(VIEWER.getName(), ADMIN.getName(), EDITOR.getName());
    }

    private final String name;
    private final Set<Authority> authorities;
    private final Set<Role> components;

    public DefaultRole(String name, Set<Authority> authorities, Set<Role> components) {
        this.name = name;
        this.authorities = authorities != null ? authorities : Collections.emptySet();
        this.components = components != null ? components : Collections.emptySet();
    }

    public DefaultRole(String name, Set<Authority> authorities) {
        this(name, authorities, Collections.emptySet());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public Set<Role> getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultRole that = (DefaultRole) o;
        return Objects.equals(name, that.name)
                && Objects.equals(authorities, that.authorities)
                && Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, authorities, components);
    }

    @Override
    public String toString() {
        return "DefaultRole[" + "name=" + name + ", authorities=" + authorities + ", components=" + components + ']';
    }
}
