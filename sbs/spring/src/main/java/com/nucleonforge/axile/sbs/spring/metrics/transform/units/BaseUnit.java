package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.sbs.spring.metrics.transform.BaseUnitValueTransformer;

/**
 * The base unit that is reported by {@link ActuatorEndpoints#SINGLE_METRIC} which we
 * know and care about. By "care" I mean that we can apply certain transformations for the
 * values that are reported by this base unit.
 * <p>
 * For instance, if the value of the given metric is
 * 123456789, and the base unit is {@link BytesMemoryBaseUnit bytes}, then we may be
 * willing to convert it onto approximately 117.7 MB (123456789 divided by 1024^2).
 * <p>
 * This "transformation" mechanism example above exists mostly for readability purposes.
 * The actual convertion is implemented via {@link BaseUnitValueTransformer}.
 *
 * @see BaseUnitValueTransformer
 * @author Mikhail Polivakha
 */
public interface BaseUnit {

    /**
     * @return An array of aliases for the given base unit. All members expected to be in lower case.
     */
    Set<String> getAliases();

    /**
     * @return The name of the base unit as to be displayed on the UI.
     */
    String getDisplayName();
}
