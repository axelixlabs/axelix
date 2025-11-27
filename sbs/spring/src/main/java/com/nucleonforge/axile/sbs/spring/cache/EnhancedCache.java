package com.nucleonforge.axile.sbs.spring.cache;

import org.springframework.cache.Cache;

/**
 * The enhanced variation of {@link Cache} to handle specific operations.
 *
 * @author Mikhail Polivakha
 */
public interface EnhancedCache extends Cache {

    /**
     * Disable the given cache. By "disabling" we mean that
     * <ul>
     *   <li>Any {@code get} variation will return {@code null}</li>
     *   <li>Any {@code put} variation will actually be no-op, i.e.
     *   not put anything into underlying delegating cache</li>
     *   <li>The {@code evict/clear/invalidate} will also be no-op, i.e. will
     *   not anyhow affect the delegate cache</li>
     * </ul>
     *
     * All the other behavior (i.e. {@link #getNativeCache()}, {@link #getName()}) remains the same.
     */
    void disable();

    /**
     * Enable this cache. By "enabling" we mean to undo all the effects
     * caused by {@link #disable()} method invocation. If the cache is not disabled,
     * this calling this method is a no-op.
     */
    void enable();

    /**
     * @return {@code true} if this cache is enabled, {@code false} otherwise.
     */
    boolean isEnabled();
}
