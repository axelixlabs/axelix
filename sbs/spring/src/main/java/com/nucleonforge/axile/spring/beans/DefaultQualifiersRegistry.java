package com.nucleonforge.axile.spring.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.NonNull;

/**
 * Default in-memory registry that stores the qualifiers of a given beans.
 *
 * @author Mikhail Polivakha
 */
public class DefaultQualifiersRegistry {

    public static final DefaultQualifiersRegistry INSTANCE = new DefaultQualifiersRegistry();

    private final ConcurrentMap<String, List<String>> cache;

    public DefaultQualifiersRegistry() {
        this.cache = new ConcurrentHashMap<>(32);
    }

    @NonNull
    public List<String> getQualifiers(@NonNull String beanName) {
        return Optional.ofNullable(cache.get(beanName)).orElse(new ArrayList<>());
    }

    public void registerQualifiers(@NonNull String beanNames, @NonNull List<String> qualifiers) {
        cache.compute(beanNames, (s, existingQualifiers) -> {
            if (existingQualifiers == null || existingQualifiers.isEmpty()) {
                existingQualifiers = new ArrayList<>();
            }
            existingQualifiers.addAll(qualifiers);
            return existingQualifiers;
        });
    }
}
