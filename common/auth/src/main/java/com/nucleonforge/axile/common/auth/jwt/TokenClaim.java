package com.nucleonforge.axile.common.auth.jwt;

/**
 * Enum representing JWT token claim keys used for consistent naming
 * of custom claims within JWT payloads.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public enum TokenClaim {
    ROLES("roles"),
    ROLE_NAME("name"),
    AUTHORITIES("authorities");

    /**
     * The string value that will be used as the key when this claim is encoded in a token.
     * <p>
     * This represents the actual key name that will appear in the serialized token (e.g., JWT).
     * For example, if the encoding is "roles", the token will contain a claim like:
     * {@code "roles": ["ADMIN", "USER"]}
     * </p>
     * <p>
     * This value is typically used during token creation and parsing to ensure consistent
     * naming of claims across the system.
     * </p>
     */
    private final String encoding;

    TokenClaim(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }
}
