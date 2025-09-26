package com.nucleonforge.axile.master.api.error.handle.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.error.ApiError;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.error.handle.ExceptionHandler;
import com.nucleonforge.axile.master.exception.ProfileSerializationException;

/**
 * {@link ExceptionHandler} for {@link ProfileSerializationException}.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@Component
public class ProfileSerializationExceptionExceptionHandler implements ExceptionHandler<ProfileSerializationException> {

    @Override
    public ResponseEntity<ApiError> handle(ProfileSerializationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleApiError("CANNOT_SERIALIZE_PROFILES"));
    }

    @Override
    public Class<ProfileSerializationException> supported() {
        return ProfileSerializationException.class;
    }
}
