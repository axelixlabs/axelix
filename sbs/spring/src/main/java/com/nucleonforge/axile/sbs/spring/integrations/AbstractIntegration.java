package com.nucleonforge.axile.sbs.spring.integrations;

/**
 * Base implementation of the {@link Integration} interface that provides common logic
 * and holds core integration parameters such as network address, protocol, and entity type.
 *
 * <p>This class is intended to be extended by specific types of integrations,
 * which may add additional behavior or properties as needed.</p>
 *
 * @since 05.07.2025
 * @author Mikhail Polivakha
 */
public abstract non-sealed class AbstractIntegration implements Integration {

    private final String networkAddress;
    private final String protocol;
    private final String entityType;

    protected AbstractIntegration(String networkAddress, String protocol, String entityType) {
        this.networkAddress = networkAddress;
        this.protocol = protocol;
        this.entityType = entityType;
    }

    @Override
    public String entityType() {
        return entityType;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String networkAddress() {
        return networkAddress;
    }
}
