package com.nucleonforge.axile.sbs.spring.metrics.transform;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BaseUnit;

/**
 * Transformed base unit.
 *
 * @param baseUnit the base unit of value
 * @param value the value
 */
public record TransformedMetricValue(BaseUnit baseUnit, double value) {}
