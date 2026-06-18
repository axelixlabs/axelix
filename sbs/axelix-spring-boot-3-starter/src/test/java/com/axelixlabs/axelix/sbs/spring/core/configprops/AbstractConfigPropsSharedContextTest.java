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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;

/**
 * Base class for the {@code configprops} integration tests.
 *
 * <p>It fixes a single, shared merged context configuration (the same {@code classes}, properties and imports)
 * so that every subclass resolves to the <strong>same cached</strong> Spring {@link ApplicationContext}. The
 * differing sanitization behavior is provided by the named service beans declared in
 * {@link ConfigPropsTestSupportConfiguration}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "management.endpoint.configprops.show-values=always",
            "axelix.prop.test.tags.environment=test",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized",
            "axelix.prop.test.enabled-contexts=user-service,payment-service",
            "axelix.prop.test.http-client.requests[0].name=user-api",
            "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axelix.prop.test.http-client.requests[0].methods[1].type=POST",
            "axelix.prop.test.http-client.requests[1].name=payment-api",
            "axelix.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axelix.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG"
        })
@Import({ConfigPropsTestSupportConfiguration.class, JwtAuthTestConfiguration.class})
public abstract class AbstractConfigPropsSharedContextTest {

    @Autowired
    protected ConfigurationPropertiesConverter converter;
}
