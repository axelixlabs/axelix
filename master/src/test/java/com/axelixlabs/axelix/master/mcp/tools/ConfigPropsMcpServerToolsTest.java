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
package com.axelixlabs.axelix.master.mcp.tools;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigPropsMcpServerTools}.
 *
 * @author Nikita Kirillov
 */
class ConfigPropsMcpServerToolsTest extends AbstractInstanceMcpServerToolsTest {

    @Test
    void returnsConfigurationPropertiesAsJson() throws InterruptedException {
        // given.
        instance.respondWith("/axelix-configprops", """
                {"beans":[{"beanName":"client","prefix":"app","properties":[
                {"key":"secret","value":"original","sensitive":true}],"inputs":[]}]}
                """);

        // when.
        String result = callTool(McpEndpoints.CONFIG_PROPS_FEED_TOOL_NAME, Map.of("instanceId", INSTANCE_ID));

        // then.
        assertThatJson(result).inPath("beans[0].properties[0].value").isEqualTo("original");
        assertThatJson(result).inPath("beans[0].properties[0].sensitive").isAbsent();
        assertThat(instance.takeRequest().getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-configprops");
    }
}
