package com.nucleonforge.axile.master.service.state;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

/**
 * Implementation of the {@link InstanceRegistry} that holds the data in the process memory.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
@Component
public class InMemoryInstanceRegistry implements InstanceRegistry {

    private final ConcurrentMap<InstanceId, Instance> source;

    public InMemoryInstanceRegistry() {
        this.source = new ConcurrentHashMap<>();
    }

    @PostConstruct
    void init() {
        String instanceId = "instanceId";
        Instance instance = new Instance(
            InstanceId.of(instanceId),
            "",
            "",
            "",
            "",
            "",
            null,
            Instance.InstanceStatus.UP,
            "http://localhost:8089/actuator");

        this.source.putIfAbsent(InstanceId.of(instanceId), instance);
    }

    @Override
    public void register(Instance instance) throws InstanceAlreadyRegisteredException {
        Instance peer = this.source.putIfAbsent(instance.id(), instance);

        if (peer != null) {
            throw new InstanceAlreadyRegisteredException();
        }
    }

    @Override
    public void deRegister(InstanceId instanceId) throws InstanceNotFoundException {
        Instance oldValue = source.remove(instanceId);

        if (oldValue == null) {
            throw new InstanceNotFoundException();
        }
    }

    @Override
    public Optional<Instance> get(InstanceId instanceId) {
        return Optional.ofNullable(source.get(instanceId));
    }

    @Override
    public Set<Instance> getAll() {
        // TODO: Is this thread safe?
        return new HashSet<>(source.values());
    }
}
