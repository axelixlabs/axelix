package com.nucleonforge.axile.spring.build;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.Instance;

/**
 * Assembles the {@link BuildInfo} for this {@link Instance}.
 *
 * @author Mikhail Polivakha
 */
public interface BuildInfoAssembler {

    /**
     * Assemble the {@link BuildInfo}.
     */
    BuildInfo assemble();
}
