package com.axelixlabs.axelix.master.autoconfiguration.mcp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static com.axelixlabs.axelix.master.autoconfiguration.mcp.McpAutoConfiguration.MCP_CONFIGURATION_PROPERTIES_PREFIX;

/**
 * A thin wrapper around Spring Boot's {@link ConditionalOnProperty} that checks if the mcp server is enabled or not.
 *
 * @author Mikhail Polivakha
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ConditionalOnProperty(prefix = MCP_CONFIGURATION_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
public @interface ConditionalOnMcpServerEnabled {
}
