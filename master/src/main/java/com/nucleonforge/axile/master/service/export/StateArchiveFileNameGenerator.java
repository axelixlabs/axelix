package com.nucleonforge.axile.master.service.export;

import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Abstraction that is capable to generate the file name for the state file archive.
 *
 * @author Mikhail Polivakha
 */
public interface StateArchiveFileNameGenerator {

    /**
     * Generation function.
     *
     * @param instanceId the id of the {@link Instance}, for which the filename is generated.
     * @return full file name (including extension) of the state archive for the {@link Instance} with the given {@link InstanceId}
     */
    String generate(InstanceId instanceId);
}
