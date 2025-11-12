package com.nucleonforge.axile.master.service.serde;

import org.jspecify.annotations.NonNull;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * {@link MessageDeserializationStrategy} for the deserialization of the {@code byte[]}
 * into the {@link Resource}.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
public abstract class BinaryResourceMessageDeserializationStrategy implements MessageDeserializationStrategy<Resource> {

    @Override
    public @NonNull Resource deserialize(byte @NonNull [] binary) throws DeserializationException {
        try {
            return new ByteArrayResource(binary) {

                @Override
                public String getFilename() {
                    return filename();
                }
            };
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }

    /**
     * @return the file name of the deserialized resource.
     */
    protected abstract String filename();
}
