package com.nucleonforge.axile.master.api.error.handle.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.error.ApiError;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.error.handle.ExceptionHandler;
import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;

/**
 * {@link ExceptionHandler} for {@link InstanceAlreadyRegisteredException}.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@Component
public class InstanceAlreadyRegisteredExceptionExceptionHandler
        implements ExceptionHandler<InstanceAlreadyRegisteredException> {

    @Override
    public ResponseEntity<ApiError> handle(InstanceAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleApiError("INSTANCE_ALREADY_REGISTERED"));
    }

    @Override
    public Class<InstanceAlreadyRegisteredException> supported() {
        return InstanceAlreadyRegisteredException.class;
    }
}
