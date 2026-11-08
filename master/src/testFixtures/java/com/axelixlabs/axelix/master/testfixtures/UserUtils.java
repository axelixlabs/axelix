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
package com.axelixlabs.axelix.master.testfixtures;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.DefaultUser;
import com.axelixlabs.axelix.master.domain.auth.OssAuthority;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.User;

/**
 * Utils for create User.
 *
 * @author Niktia Kirillov
 * @author Mikhail Polivakha
 */
public class UserUtils {

    private static final Random RANDOM = new Random();

    public static User fromAuthorities(OssAuthority... authorities) {
        Set<Authority> authoritySet = Set.of(authorities);
        Role role = new DefaultRole(pseudoRandomStirng(), authoritySet);
        return new PasswordlessUser(pseudoRandomStirng(), pseudoRandomStirng(), Set.of(role));
    }

    public static User fromRoles(Role... roles) {
        return new PasswordlessUser(
                pseudoRandomStirng(), pseudoRandomStirng(), Arrays.stream(roles).collect(Collectors.toSet()));
    }

    public static PasswordlessUser passwordless(String username, Set<Role> roles) {
        return new PasswordlessUser(pseudoRandomStirng(), username, roles);
    }

    public static DefaultUser withPassword(String username, String password, Set<Role> roles) {
        return new DefaultUser(pseudoRandomStirng(), username, password, roles);
    }

    public static String pseudoRandomStirng() {
        return RANDOM.ints(12, 'a', 'z' + 1) // Generates lowercase letters
                .mapToObj(i -> String.valueOf((char) i))
                .collect(Collectors.joining());
    }
}
