package com.nucleonforge.axile.master.service.serde;

import org.jspecify.annotations.NonNull;

/**
 * The strategy for serializing any given object into byte array.
 *
 * @author Mikhail Polivakha
 */
public interface MessageSerializationStrategy {

    /**
     * Perform actual serialization.
     *
     * @param object the object to be serialized.
     * @throws SerializationException in case the error happened during serialization.
     * @return the serialized version of an object.
     */
    byte @NonNull [] serialize(@NonNull Object object) throws SerializationException;
}
