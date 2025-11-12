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

    /**
     * This filename extension is valid only for HotSpot heapdump format.
     * <p>
     * Although, in 99% of cases hprof is the format of the actual binary
     * deserialized file, it might still be that someone is using OpenJ9 for
     * instance or anything, and head dump format would differ from hprof.
     */
    @Override
    protected String filename() {
        return "heapdump.hprof";
    }
}
