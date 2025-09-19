package com.nucleonforge.axile.master.service.serde;

import org.jspecify.annotations.NonNull;

/**
 * A NoOp {@link MessageDeserializationStrategy}, that just translates the value as it is.
 *
 * @author Mikhail Polivakha
 */
public class NoOpMessageDeserializationStrategy implements MessageDeserializationStrategy<byte[]> {

    public static final NoOpMessageDeserializationStrategy INSTANCE = new NoOpMessageDeserializationStrategy();

    @Override
    public byte @NonNull [] deserialize(byte @NonNull [] binary) throws DeserializationException {
        return binary;
    }
}
