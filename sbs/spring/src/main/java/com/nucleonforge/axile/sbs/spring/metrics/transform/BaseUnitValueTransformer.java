package com.nucleonforge.axile.sbs.spring.metrics.transform;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BaseUnit;

/**
 * Transforms the value of the given metrics. On the conceptual level
 * it acts just like a {@link java.util.function.Function}. It
 * is also a strategy interface of some sort, see the {@link #supports()} method.
 *
 * @author Mikhail Polivakha
 */
public interface BaseUnitValueTransformer {

    /**
     * Actual transformation function. Transforms the given double (which
     * is reported in the base unit as reported by {@link #supports()}) into
     * another {@link TransformedMetricValue}.
     *
     * @param value value to transform
     * @return transformed value
     */
    TransformedMetricValue transform(double value);

    /**
     * @return TransformableBaseUnit for which the current {@link BaseUnitValueTransformer} is responsible.
     */
    @NonNull
    BaseUnit supports();
}
