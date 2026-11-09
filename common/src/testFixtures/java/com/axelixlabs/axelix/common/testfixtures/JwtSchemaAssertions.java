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
package com.axelixlabs.axelix.common.testfixtures;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.Schema;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.ValidationFailure;
import com.github.erosb.jsonsKema.Validator;

/** Assertions against the JWT wire contract, independent of generated models. */
public final class JwtSchemaAssertions {
    private JwtSchemaAssertions() {}

    public static void assertJwtPayloadMatchesSchema(String token, String schemaName) {
        try (InputStream input =
                Objects.requireNonNull(JwtSchemaAssertions.class.getResourceAsStream("/contract/jwt/jwt-token.yaml"))) {
            JsonNode document = new YAMLMapper().readTree(input);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode schemaDocument = mapper.createObjectNode();
            schemaDocument.set("components", document.get("components"));
            schemaDocument.put("$ref", "#/components/schemas/" + schemaName);
            Schema schema = new SchemaLoader(new JsonParser(schemaDocument.toString()).parse()).load();
            String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
            ValidationFailure errors = Validator.forSchema(schema).validate(new JsonParser(payload).parse());
            if (errors != null) {
                throw new AssertionError("JWT payload violates " + schemaName + ": " + errors);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
