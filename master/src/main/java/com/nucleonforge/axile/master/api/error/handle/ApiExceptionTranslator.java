package com.nucleonforge.axile.master.api.error.handle;

import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.api.error.ApiError;

/**
 * Common interface for any classes that are capable to translate the given {@link Exception} to
 * concrete abstraction over the Http Response, i.e. {@link org.springframework.http.ResponseEntity}.
 *
 * @author Mikhail Polivakha
 */
public interface ApiExceptionTranslator {

    /**
     * Translate given Exception.
     *
     * @param e exception to translate. Cannot be null
     * @return Spring's abstraction over http response
     */
    ResponseEntity<ApiError> translateException(Exception e);
}
