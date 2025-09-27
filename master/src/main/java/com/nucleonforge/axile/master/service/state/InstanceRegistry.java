package com.nucleonforge.axile.master.service.state;

import java.util.Optional;
import java.util.Set;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.InstanceReference;
import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

/**
 * Central registry of all the {@link InstanceReference instances} that this Axile Master instance is aware about.
 * It is guaranteed that all the instances inside this registry have the unique instance id. The implementations
 * must be thread safe.
 *
 * @see InstanceReference
 * @author Mikhail Polivakha
 */
public interface InstanceRegistry {

    /**
     * Register the given instance inside the registry. In case the {@link InstanceReference} with this ID
     * is already present then re-registration must not happen and the exception must be thrown.
     *
     * @param instanceReference the instance to be registered
     * @throws InstanceAlreadyRegisteredException in case the {@link InstanceReference} with
     *         the same id is already present in the registry
     */
    void register(InstanceReference instanceReference) throws InstanceAlreadyRegisteredException;

    /**
     * Deregister the {@link InstanceReference} by the instanceId.
     *
     * @param instanceId the id of the instance that is supposed to be deregistered.
     * @throws InstanceNotFoundException in case such an {@link InstanceReference} is not found.
     */
    void deRegister(InstanceId instanceId) throws InstanceNotFoundException;

    /**
     * Get {@link InstanceReference} by its id.
     *
     * @param instanceId the id of the instance to get.
     * @return Optional wrapping an {@link InstanceReference} that is identified by
     *         given {@code instanceId} an empty {@link Optional} otherwise.
     */
    Optional<InstanceReference> get(InstanceId instanceId);

    /**
     * Get all instances that are managed by this registry.
     *
     * @return all instances that are managed by this registry.
     */
    Set<InstanceReference> getAll();
}
