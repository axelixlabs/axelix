package com.nucleonforge.axile.master.exception;

import com.nucleonforge.axile.master.domain.Instance;
import com.nucleonforge.axile.master.service.InstanceRegistry;

/**
 * Typically thrown by the {@link InstanceRegistry} in case the given {@link Instance}
 * is not found.
 *
 * @author Mikhail Polivakha
 */
public class NoSuchInstanceException extends RuntimeException {}
