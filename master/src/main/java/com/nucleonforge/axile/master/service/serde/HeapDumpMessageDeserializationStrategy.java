package com.nucleonforge.axile.master.service.serde;

import org.springframework.stereotype.Component;

/**
 * {@link BinaryResourceMessageDeserializationStrategy} for heapdump.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Component
public class HeapDumpMessageDeserializationStrategy extends BinaryResourceMessageDeserializationStrategy {

    @Override
    protected String filename() {
        return "heapdump.hprof";
    }
}
