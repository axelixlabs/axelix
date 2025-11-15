package com.nucleonforge.axile.master.autoconfiguration.auth;

import java.time.Duration;

import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;

@SuppressWarnings("NullAway")
public class JwtProperties {

    private JwtAlgorithm algorithm;
    private String signingKey;
    private Duration lifespan;

    public JwtProperties setAlgorithm(JwtAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public JwtProperties setSigningKey(String signingKey) {
        this.signingKey = signingKey;
        return this;
    }

    public JwtProperties setLifespan(Duration lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    public JwtAlgorithm getAlgorithm() {
        return algorithm;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public Duration getLifespan() {
        return lifespan;
    }
}
