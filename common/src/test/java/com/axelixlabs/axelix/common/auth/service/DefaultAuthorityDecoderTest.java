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
package com.axelixlabs.axelix.common.auth.service;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.OssAuthority;
import com.axelixlabs.axelix.common.auth.service.DefaultAuthorityDecoder.AuthoritiesContributor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultAuthorityDecoder}.
 *
 * @author Mikhail Polivakha
 */
class DefaultAuthorityDecoderTest {

    @ParameterizedTest
    @EnumSource(OssAuthority.class)
    void shouldDecodeEveryDefaultAuthority(OssAuthority authority) {
        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(null);

        Authority decoded = decoder.decode(authority.getName());

        assertThat(decoded).isEqualTo(authority);
    }

    @Test
    void shouldReturnNull_WhenNameIsUnknown() {
        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(null);

        assertThat(decoder.decode("NON_EXISTENT_AUTHORITY")).isNull();
    }

    @Test
    void shouldDecodeDefaultAuthorities_WhenContributorIsNull() {
        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(null);

        assertThat(decoder.decode(OssAuthority.USERS_VIEW.getName())).isSameAs(OssAuthority.USERS_VIEW);
    }

    @Test
    void shouldDecodeDefaultAuthorities_WhenContributorContributesNull() {
        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(() -> null);

        assertThat(decoder.decode(OssAuthority.USERS_VIEW.getName())).isSameAs(OssAuthority.USERS_VIEW);
    }

    @Test
    void shouldDecodeDefaultAuthorities_WhenContributorContributesEmptySet() {
        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(Set::of);

        assertThat(decoder.decode(OssAuthority.USERS_VIEW.getName())).isSameAs(OssAuthority.USERS_VIEW);
    }

    @Test
    void shouldDecodeContributedAuthority() {
        Authority contributed = () -> "CUSTOM_AUTHORITY";
        AuthoritiesContributor contributor = () -> Set.of(contributed);

        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(contributor);

        assertThat(decoder.decode("CUSTOM_AUTHORITY")).isEqualTo(contributed);
    }

    @Test
    void shouldDecodeBothDefaultAndContributedAuthorities() {
        Authority contributed = () -> "CUSTOM_AUTHORITY";
        AuthoritiesContributor contributor = () -> Set.of(contributed);

        DefaultAuthorityDecoder decoder = new DefaultAuthorityDecoder(contributor);

        assertThat(decoder.decode("CUSTOM_AUTHORITY")).isEqualTo(contributed);
        assertThat(decoder.decode(OssAuthority.CACHES_CLEAR.getName())).isEqualTo(OssAuthority.CACHES_CLEAR);
    }
}
