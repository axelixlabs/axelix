package com.nucleonforge.axile.master.api.error.handle;

import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.api.error.ApiError;

public interface ExceptionHandler<T extends Exception> {

    ResponseEntity<ApiError> handle(T exception);

    Class<T> supported();
}
