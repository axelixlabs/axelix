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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.axelixlabs.axelix.common.auth.contract.token.AccessToken;

/**
 * Jackson view of the access-token contract. Unknown claims are ignored so a token minted by a
 * newer peer can still be read for as long as the compatibility window requires it.
 *
 * @author Mikhail Polivakha
 */
final class AccessTokenJson {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private AccessTokenJson() {}

    static String write(AccessToken token) throws JsonProcessingException {
        return MAPPER.writeValueAsString(token);
    }

    static AccessToken read(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, AccessToken.class);
    }
}
