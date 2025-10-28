package com.nucleonforge.axile.master.service.export;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Default implementation of {@link StateArchiveFileNameGenerator}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultStateArchiveFileNameGenerator implements StateArchiveFileNameGenerator {

    public static final String STATE_ARCHIVE_FILE_TEMPLATE = "instance-state-%s-%s.zip";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'mm-HH-ss");

    // spotless:off
    @Override
    public String generate(InstanceId instanceId) {
        return STATE_ARCHIVE_FILE_TEMPLATE.formatted(
            instanceId.instanceId(),
            FORMATTER.format(Instant.now().atZone(ZoneOffset.systemDefault()))
        );
    }
    // spotless:on
}
