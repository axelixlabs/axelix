package com.nucleonforge.axile.master.api.error.handle.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.error.ApiError;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.error.handle.ExceptionHandler;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

/**
 * {@link ExceptionHandler} for {@link InstanceNotFoundException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class InstanceNotFoundExceptionExceptionHandler implements ExceptionHandler<InstanceNotFoundException> {

    @Override
    public ResponseEntity<ApiError> handle(InstanceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleApiError("INSTANCE_NOT_FOUND"));
    }

    @Override
    public Class<InstanceNotFoundException> supported() {
        return InstanceNotFoundException.class;
    }
}
