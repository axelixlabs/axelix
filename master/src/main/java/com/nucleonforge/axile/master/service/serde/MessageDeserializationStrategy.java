package com.nucleonforge.axile.master.service.serde;

import org.jspecify.annotations.NonNull;

/**
 * Deserializer for a specific object.
 *
 * @param <T> type of the object to be deserialized.
 * @author Mikhail Polivakha
 */
public interface MessageDeserializationStrategy<T> {

    /**
     * Perform actual deserialization.
     *
     * @param binary the byte array to be deserialized.
     * @throws DeserializationException in case the error happened during deserialization.
     * @return the deserialized object, guaranteed to be not null.
     */
    @NonNull
    T deserialize(byte @NonNull [] binary) throws DeserializationException;
}
