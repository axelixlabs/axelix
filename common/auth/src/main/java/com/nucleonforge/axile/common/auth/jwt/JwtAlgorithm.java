package com.nucleonforge.axile.common.auth.jwt;

/**
 * Enum representing supported JWT signing algorithms,
 * along with their required minimum key lengths.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public enum JwtAlgorithm {
    HMAC256(32, "HS256"),
    HMAC384(48, "HS384"),
    HMAC512(64, "HS512");

    /**
     * Minimum required key length in bytes for the algorithm.
     */
    private final int minKeyLength;

    /**
     * The standard JWT algorithm name.
     */
    private final String algorithmName;

    JwtAlgorithm(int minKeyLength, String algorithmName) {
        this.minKeyLength = minKeyLength;
        this.algorithmName = algorithmName;
    }

    public int getMinKeyLength() {
        return minKeyLength;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }
}
