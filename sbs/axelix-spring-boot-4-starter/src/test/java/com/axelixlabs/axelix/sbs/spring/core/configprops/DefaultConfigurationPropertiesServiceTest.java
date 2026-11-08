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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
public class DefaultConfigurationPropertiesServiceTest extends AbstractConfigPropsSharedContextTest {

    @Autowired
    private ConfigurationPropertiesService configurationPropertiesService;

    @Test
    void shouldReturnConfigurationPropertiesWithoutSanitization() {
        // when.
        ConfigurationPropertiesFeed configProps = configurationPropertiesService.getConfigProps();

        // then.
        Set<@Nullable String> values = configProps.getBeans().stream()
                .flatMap(bean -> bean.getProperties().stream())
                .map(ConfigurationPropertiesEntry::getValue)
                .collect(Collectors.toSet());

        assertThat(values).contains("toBeSanitized").doesNotContain("******");
    }
}
