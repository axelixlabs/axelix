package com.nucleonforge.axile.master.exception;

import com.nucleonforge.axile.master.instance.Instance;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * Typically thrown by the {@link InstanceRegistry} in case the given {@link Instance} cannot be registered
 * due to the conflict - the same instance is already registered.
 *
 * @author Mikhail Polivakha
 */
public class InstanceAlreadyRegisteredException extends RuntimeException {}
