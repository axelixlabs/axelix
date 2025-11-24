package com.nucleonforge.axile.master.service.export;

import java.util.List;

/**
 * The request for export of particular component states.
 *
 * @author Mikhail Polivakha
 */
public record StateExport(List<StateComponentSettings> components) {}
