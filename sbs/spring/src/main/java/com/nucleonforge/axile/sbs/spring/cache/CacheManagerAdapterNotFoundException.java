package com.nucleonforge.axile.sbs.spring.cache;

/**
 * Exception thrown when a requested CacheManagerAdapter is not found in the application context.
 *
 * @since 25.11.2025
 * @author Nikita Kirillov
 */
public class CacheManagerAdapterNotFoundException extends RuntimeException {

    public CacheManagerAdapterNotFoundException(String message) {
        super(message);
    }
}
