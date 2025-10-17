package com.nucleonforge.axile.master.service.state;

import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Central registry of all the {@link Instance instances} that this Axile Master instance is aware about.
 * It is guaranteed that all the instances inside this registry have the unique instance id. The implementations
 * must be thread safe.
 *
 * @see Instance
 * @author Mikhail Polivakha
 */
public interface InstanceRegistry {

    /**
     * Register the given instance inside the registry. In case the {@link Instance} with this ID
     * is already present then re-registration must not happen and the exception must be thrown.
     *
     * @param instance the instance to be registered
     * @throws InstanceAlreadyRegisteredException in case the {@link Instance} with
     *         the same id is already present in the registry
     */
    void register(Instance instance) throws InstanceAlreadyRegisteredException;

    /**
     * Deregister the {@link Instance} by the instanceId.
     *
     * @param instanceId the id of the instance that is supposed to be deregistered.
     * @throws InstanceNotFoundException in case such an {@link Instance} is not found.
     */
    void deRegister(InstanceId instanceId) throws InstanceNotFoundException;

    /**
     * Get {@link Instance} by its id.
     *
     * @param instanceId the id of the instance to get.
     * @return Optional wrapping an {@link Instance} that is identified by
     *         given {@code instanceId} an empty {@link Optional} otherwise.
     */
    Optional<Instance> get(InstanceId instanceId);

    /**
     * Get all instances that are managed by this registry.
     *
     * @return all instances that are managed by this registry.
     */
    @NonNull
    Set<Instance> getAll();
}
