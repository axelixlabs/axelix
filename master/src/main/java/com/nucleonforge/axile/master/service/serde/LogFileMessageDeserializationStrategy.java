package com.nucleonforge.axile.master.service.serde;

import org.springframework.stereotype.Component;

/**
 * {@link BinaryResourceMessageDeserializationStrategy} for logfile.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Component
public class LogFileMessageDeserializationStrategy extends BinaryResourceMessageDeserializationStrategy {

    @Override
    protected String filename() {
        return "application.log";
    }
}
