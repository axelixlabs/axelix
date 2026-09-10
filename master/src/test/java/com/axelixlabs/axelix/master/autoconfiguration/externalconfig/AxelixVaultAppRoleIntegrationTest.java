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
package com.axelixlabs.axelix.master.autoconfiguration.externalconfig;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test proving that Axelix Master, when pointed at Hashicorp Vault through the
 * {@code axelix.master.external-config.spring-cloud-vault.*} properties, authenticates using the AppRole
 * method and fetches a real secret into its {@link Environment} during start-up.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest
class AxelixVaultAppRoleIntegrationTest {

    private static final String VAULT_CONFIG_PREFIX = AxelixVaultProperties.AXELIX_PREFIX;

    private static final String ROOT_TOKEN = "axelix-test-root-token";
    private static final String APP_ROLE_NAME = "axelix-master";

    private static final String SEEDED_MCP_PROPERTY = "axelix.master.mcp-server.enabled";
    private static final String SEEDED_STATIC_RESOURCES_PROPERTY = "axelix.master.web.static-resources.location";

    private static final VaultContainer<?> VAULT = new VaultContainer<>(DockerImageName.parse("hashicorp/vault:1.21.4"))
            .withVaultToken(ROOT_TOKEN)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("vault/axelix-policy.hcl"), "/vault/policies/axelix-policy.hcl")
            .withInitCommand(
                    "kv put secret/axelix " + SEEDED_MCP_PROPERTY + "=false " + SEEDED_STATIC_RESOURCES_PROPERTY
                            + "=classpath:/spa/",
                    "policy write axelix-policy /vault/policies/axelix-policy.hcl",
                    "auth enable approle",
                    "write auth/approle/role/" + APP_ROLE_NAME
                            + " token_policies=axelix-policy token_ttl=1h token_max_ttl=4h secret_id_ttl=10m");

    static {
        VAULT.start();

        String roleId = execVault("read", "-field=role_id", "auth/approle/role/" + APP_ROLE_NAME + "/role-id");
        String secretId =
                execVault("write", "-field=secret_id", "-f", "auth/approle/role/" + APP_ROLE_NAME + "/secret-id");

        System.setProperty("spring.config.import", "optional:vault://");
        System.setProperty("spring.application.name", "axelix");
        System.setProperty(VAULT_CONFIG_PREFIX + ".uri", VAULT.getHttpHostAddress());
        System.setProperty(VAULT_CONFIG_PREFIX + ".authentication", "APPROLE");
        System.setProperty(VAULT_CONFIG_PREFIX + ".app-role.role", APP_ROLE_NAME);
        System.setProperty(VAULT_CONFIG_PREFIX + ".app-role.role-id", roleId);
        System.setProperty(VAULT_CONFIG_PREFIX + ".app-role.secret-id", secretId);
        System.setProperty(VAULT_CONFIG_PREFIX + ".kv.enabled", "true");
        System.setProperty(VAULT_CONFIG_PREFIX + ".kv.backend", "secret");
        // Vault dev mode always mounts 'secret/' as a KV v2 backend; pinning the version here skips
        // spring-cloud-vault's own auto-detection probe, which the AppRole policy above does not grant.
        System.setProperty(VAULT_CONFIG_PREFIX + ".kv.backend-version", "2");
        System.setProperty(VAULT_CONFIG_PREFIX + ".kv.application-name", "axelix");
    }

    private static String execVault(String... args) {
        try {
            String[] command = new String[args.length + 1];
            command[0] = "vault";
            System.arraycopy(args, 0, command, 1, args.length);

            Container.ExecResult result = VAULT.execInContainer(command);

            if (result.getExitCode() != 0) {
                throw new IllegalStateException("'vault " + String.join(" ", args) + "' failed: " + result.getStderr());
            }
            return result.getStdout().trim();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to run 'vault " + String.join(" ", args) + "'", e);
        }
    }

    @Autowired
    private Environment environment;

    @AfterAll
    static void tearDown() {
        System.clearProperty("spring.config.import");
        System.clearProperty("spring.application.name");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".uri");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".authentication");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".app-role.role");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".app-role.role-id");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".app-role.secret-id");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".kv.enabled");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".kv.backend");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".kv.backend-version");
        System.clearProperty(VAULT_CONFIG_PREFIX + ".kv.application-name");
        VAULT.stop();
    }

    @Test // GH-1489
    void shouldAuthenticateViaAppRoleAndFetchConfigurationFromVault() {
        assertThat(environment.getProperty(SEEDED_MCP_PROPERTY)).isEqualTo("false");
        assertThat(environment.getProperty(SEEDED_STATIC_RESOURCES_PROPERTY)).isEqualTo("classpath:/spa/");
    }
}
