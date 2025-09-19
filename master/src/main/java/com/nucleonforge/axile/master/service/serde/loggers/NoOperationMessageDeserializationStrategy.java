package com.nucleonforge.axile.master.service.serde.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.service.serde.DeserializationException;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;

/**
 * {@link MessageDeserializationStrategy} for {@link Object}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class NoOperationMessageDeserializationStrategy implements MessageDeserializationStrategy<Object> {

    @Override
    public @NonNull Object deserialize(byte @NonNull [] binary) throws DeserializationException {
        return new Object();
    }
}
